package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.flight;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.JetpackParticles;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Permissions;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight.FlightCheck;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The repeating fuel tick: every round it charges what the player is wearing, decides whether the
 * flight continues, and starts the effects once it does.
 * <p>
 * Owns the {@link BukkitTask}. Stopping the task and deciding that there is nothing left to fly with
 * are the same decision, so they live together.
 */
final class FlightTicker {

    private final JavaPlugin plugin;
    private final ConfigRepository configs;
    private final MessageSink messages;
    private final JetpackParticles particles;
    private final Log log;

    FlightTicker(@NotNull JavaPlugin plugin,
                 @NotNull ConfigRepository configs,
                 @NotNull MessageSink messages,
                 @NotNull JetpackParticles particles,
                 @NotNull Log log) {
        this.plugin = plugin;
        this.configs = configs;
        this.messages = messages;
        this.particles = particles;
        this.log = log;
    }

    /**
     * Starts the loop for a session that is about to fly.
     *
     * @param session      the session whose jetpack is already selected
     * @param halt         stop the flight, without telling the player
     * @param haltReported stop the flight and report it - the path taken when the tick itself failed
     */
    void start(@NotNull FlightSession session, @NotNull Runnable halt, @NotNull Runnable haltReported) {
        val delayInSec = session.getJetpack().getFuel().getBurnRate();

        // main thread: the tick touches inventory/equipment/player state
        Bukkit.getScheduler().runTaskTimer(plugin, bukkitTask -> {
            if (session.getBurnTask() == null) session.setBurnTask(bukkitTask);
            if (!session.isStarting() && !session.isActive()) {
                bukkitTask.cancel();
                return;
            }
            Catcher.create(() -> burnAction(session, halt)).onSuccess(check -> {
                if (check == FlightCheck.NO_PERMISSION) {
                    messages.send(session.getPlayer(), configs.message().getNoPermission());
                    halt.run();
                    return;
                }
                if (check == FlightCheck.BLOCKED) {
                    halt.run();
                    return;
                }
                if (!session.isStarting()) return;
                session.setActive(true);
                val player = session.getPlayer();
                player.setAllowFlight(true);
                player.setFlySpeed(session.getJetpack().getSpeed() / 10.0f);
                messages.send(player, configs.message().getTurnOn());

                particles.start(session);
            }).onFailure(error -> {
                haltReported.run();
                log.debug("Error: %s", error);
            }).onCompleted(() -> {
                if (session.isStarting())
                    session.setStarting(false);
            });
        }, 0L, delayInSec * 20L);
    }

    /**
     * One round: charge fuel for whatever the player is wearing.
     *
     * @return {@link FlightCheck#ALLOWED} to keep flying, otherwise the refusal reason
     */
    private @NotNull FlightCheck burnAction(@NotNull FlightSession session, @NotNull Runnable halt) {
        val jetpack = session.getJetpack();
        assert jetpack != null;
        val player = session.getPlayer();

        if (!Permissions.hasRawPermission(player, jetpack.getPermission()))
            return FlightCheck.NO_PERMISSION;

        if (!session.isStarting() && ((LivingEntity) player).isOnGround() && !player.isFlying())
            return FlightCheck.ALLOWED;

        if (jetpack.isRunInOffHandOnly()) {
            val offHandItem = player.getInventory().getItemInOffHand();
            val check = session.isJetpack(offHandItem, !session.isStarting());
            if (check == FlightCheck.NO_PERMISSION || check == FlightCheck.BLOCKED) return check;
            if (check == FlightCheck.NOT_A_JETPACK) {
                halt.run();
                messages.send(player, configs.message().getDetached());
            }
            return FlightCheck.ALLOWED;
        }

        val equipment = player.getEquipment();
        assert equipment != null;
        for (@Nullable ItemStack armor : equipment.getArmorContents()) {
            val check = session.isJetpack(armor, !session.isStarting());
            if (check == FlightCheck.ALLOWED) return FlightCheck.ALLOWED;
            if (check != FlightCheck.NOT_A_JETPACK) return check;
        }

        // decide the wording before stopping: stopping resets the flight flags this reads
        val notice = player.isFlying()
                ? configs.message().getDetached()
                : configs.message().getTurnOff();
        halt.run();
        messages.send(player, notice);
        return FlightCheck.ALLOWED;
    }

    /**
     * Cancels the loop, if it ever started, and stops the trail that belongs to it.
     * <p>
     * The particles are owned by this ticker - it is the only thing that starts them - so it is also
     * what stops them. Leaving that out loses the trail on a cancelled tick.
     */
    void stop(@Nullable BukkitTask task) {
        if (task != null) task.cancel();
        particles.stop();
    }

}
