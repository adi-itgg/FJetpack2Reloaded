package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application;

import lombok.val;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.flight.FlightSession;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import static java.lang.Math.sin;

/** Trail effect of one flying session. */
public class JetpackParticles {

    private final JavaPlugin plugin;
    private final ServerVersion version;
    private final Log log;

    private BukkitTask task;

    public JetpackParticles(@NotNull JavaPlugin plugin, @NotNull ServerVersion version, @NotNull MessageSink messages) {
        this.plugin = plugin;
        this.version = version;
        this.log = new Log(getClass(), messages);
    }

    public void start(@NotNull FlightSession session) {
        log.log("starting particle effect");
        val jetpack = session.getJetpack();
        if (jetpack == null || jetpack.getParticle() == null || !jetpack.getParticle().isEnable()) return;
        val effectName = jetpack.getParticle().getEffect().toUpperCase().trim();
        val effect = Catcher.create(() ->
                version.getServerVersion() > 8 ? Particle.valueOf(effectName) : Effect.valueOf(effectName)
        ).onFailure(error -> {
            log.log("&cInvalid particle effect name: %s - details: %s", jetpack.getParticle().getEffect(), error);
        }).getOrDefault(version.getServerVersion() > 8 ? Particle.CLOUD : Effect.SMOKE);
        log.log("creating task for particle effect");
        // main thread: spawnParticle / playEffect are not thread safe
        Bukkit.getScheduler().runTaskTimer(plugin, bukkitTask -> {
            // the Bukkit API returns void here, so the task handle is captured on its first run
            if (task == null) task = bukkitTask;
            if (!session.isActive()) {
                bukkitTask.cancel();
                return;
            }
            render(session, effect);
        }, 0L, jetpack.getParticle().getDelay());
    }

    public void stop() {
        if (task != null)
            task.cancel();
        task = null;
    }

    private void render(@NotNull FlightSession session, @NotNull Object effect) {
        val player = session.getPlayer();
        val jetpack = session.getJetpack();
        if (jetpack == null || jetpack.getParticle() == null) return;
        if (!session.isActive() || !player.isFlying() || ((LivingEntity) player).isOnGround()) return;

        val location = player.getLocation();
        if (version.getServerVersion() <= 8 && effect instanceof Effect legacy) {
            player.getWorld().playEffect(
                    location.add(0.0, 0.8, 0.0),
                    legacy,
                    0
            );
            return;
        }
        if (!(effect instanceof Particle particle)) return;
        val newZ = ((Double) (0.1 *
                sin(Math.toRadians(((Float) (player.getLocation().getYaw() + 270.0f)).doubleValue()))
        )).floatValue();
        player.getWorld().spawnParticle(
                particle,
                location.getX() + newZ,
                location.getY() + 0.8,
                location.getZ() + newZ,
                jetpack.getParticle().getAmount(),
                0.0,
                -0.2,
                0.0
        );
    }

}
