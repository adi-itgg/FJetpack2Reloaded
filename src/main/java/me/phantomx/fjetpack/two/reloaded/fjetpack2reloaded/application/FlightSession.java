package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application;

import lombok.*;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ItemTags;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.Placeholder;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight.FlightCheck;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight.ItemDisposition;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.fuel.FuelPolicy;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.fuel.FuelStop;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.ActionResultReturnHandler;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Live state and flight lifecycle of one player.
 * <p>
 * Two steps, kept apart on purpose: {@link #preconditions} answers "may this stack be used at all",
 * {@link #isJetpack} then charges the fuel of one tick and performs the side effects.
 */
@Data
public class FlightSession {

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final PlayerRegistry registry;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final JavaPlugin plugin;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final ConfigRepository configs;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final MessageSink messages;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final ItemTags itemTags;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final ProtectionGuard protection;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final ItemFactory items;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final ServerVersion version;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final JetpackParticles particles;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final Log log;

    private boolean isActive;
    private @NonNull Player player;
    @Setter(AccessLevel.PRIVATE)
    private @Nullable Jetpack jetpack;
    @Setter(AccessLevel.PRIVATE)
    private @Nullable BukkitTask burnTask;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean starting;
    @Setter(AccessLevel.NONE)
    private int jetpackUniqueId;

    public FlightSession(@NotNull Player player,
                         @NotNull PlayerRegistry registry,
                         @NotNull JavaPlugin plugin,
                         @NotNull ConfigRepository configs,
                         @NotNull MessageSink messages,
                         @NotNull ItemTags itemTags,
                         @NotNull ProtectionGuard protection,
                         @NotNull ItemFactory items,
                         @NotNull ServerVersion version) {
        this.player = player;
        this.registry = registry;
        this.plugin = plugin;
        this.configs = configs;
        this.messages = messages;
        this.itemTags = itemTags;
        this.protection = protection;
        this.items = items;
        this.version = version;
        this.particles = new JetpackParticles(plugin, version, messages);
        this.log = new Log(getClass(), messages);
    }

    public boolean isAllowedGriefPrevention() {
        if (jetpack == null) return true;
        return protection.allowsGriefPrevention(player, jetpack);
    }

    public boolean isAllowedSuperiorSkyblock() {
        if (jetpack == null) return true;
        return protection.allowsSuperiorSkyblock(player, jetpack);
    }

    @SneakyThrows
    public void updateActiveJetpackArmorEquipment(@NotNull ActionResultReturnHandler<@NotNull ItemStack, @Nullable ItemStack> action) {
        val equipment = player.getEquipment();
        if (equipment == null) return;
        val size = equipment.getArmorContents().length;
        val armorArray = new ItemStack[size];
        val iterator = Arrays.stream(equipment.getArmorContents()).iterator();
        var i = 0;
        while (iterator.hasNext()) {
            var item = iterator.next();
            if (item != null && itemTags.isActiveJetpack(item, jetpackUniqueId)) {
                if (version.getServerVersion() > 16) {
                    val itemMeta = item.getItemMeta();
                    if (itemMeta != null && jetpack != null) {
                        itemMeta.setUnbreakable(jetpack.isUnbreakable());
                        item.setItemMeta(itemMeta);
                    }
                }
                item = action.action(item);
            }
            armorArray[i] = item;
            i++;
        }
        equipment.setArmorContents(armorArray);
    }

    private void onOutOfFuel() {
        messages.send(player, configs.message().getOutOfFuel());
        if (jetpack == null || jetpack.getOnEmptyFuel() == ItemDisposition.NONE) return;
        if (Permissions.hasRawPermission(player, jetpack.getPermission(Permissions.PERMISSION_KEEP_ON_EMPTY_SUFFIX))) return;
        // out-of-fuel disposition, per jetpack.yml OnEmptyFuel (was: OnDeath - fixed)
        updateActiveJetpackArmorEquipment(item -> switch (jetpack.getOnEmptyFuel()) {
            case NONE -> item;
            case REMOVE -> {
                messages.send(player, configs.message().getOnEmptyFuelRemoved());
                yield null;
            }
            case DROP -> {
                player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
                messages.send(player, configs.message().getOnEmptyFuelDropped());
                yield null;
            }
        });
    }

    public @NotNull FlightCheck isJetpack(@Nullable ItemStack stack, boolean consumeFuel) {
        return isJetpack(stack, consumeFuel, false);
    }

    /**
     * Can the player fly with this item, and charge the fuel of one tick?
     *
     * @return the refusal reason, or {@link FlightCheck#ALLOWED}; side effects (fuel, item tags,
     * messages, worn-armor rewrite) only happen when allowed
     */
    public @NotNull FlightCheck isJetpack(@Nullable ItemStack stack, boolean consumeFuel, boolean updateCurrentJetpack) {
        val pre = preconditions(stack);
        if (pre.check() != FlightCheck.ALLOWED) return pre.check();
        val jetpack = pre.jetpack();
        assert jetpack != null;

        var fuel = itemTags.getFuelValue(stack);
        // item without a fuel tag: not a usable jetpack, never destroy it
        if (fuel == null) return FlightCheck.NOT_A_JETPACK;

        val settlement = FuelPolicy.settle(
                fuel,
                jetpack.getFuel().getCost(),
                jetpack.getFuel().getSprintCost(),
                Permissions.hasRawPermission(player, jetpack.getPermission() + Permissions.PERMISSION_BYPASS_FUEL_SUFFIX),
                Permissions.hasRawPermission(player, jetpack.getPermission() + Permissions.PERMISSION_BYPASS_FUEL_SPRINT_SUFFIX),
                player.isSprinting(),
                jetpack.getFuel().getWarnRunOutBelow()
        );

        if (settlement.stop() != FuelStop.NONE) {
            items.updateDisplayItem(jetpack, stack, settlement.fuel());
            onOutOfFuel();
            return FlightCheck.BLOCKED;
        }
        fuel = settlement.fuel();

        if (updateCurrentJetpack)
            setJetpack(jetpack);

        if (!consumeFuel) {
            jetpackUniqueId = itemTags.newItemId();
            stack = itemTags.setActiveJetpack(stack, jetpackUniqueId);
            @NotNull ItemStack finalArmor = stack;
            updateActiveJetpackArmorEquipment(item -> finalArmor);
            return FlightCheck.ALLOWED;
        }

        if (settlement.warn())
            messages.send(player,
                    configs.message().getWarnRunOutBelow().replace(Placeholder.AMOUNT, String.valueOf(fuel))
            );

        items.updateDisplayItem(jetpack, stack, fuel);
        stack = itemTags.setFuelValue(stack, fuel);
        @NotNull ItemStack finalArmor = stack;
        updateActiveJetpackArmorEquipment(item -> finalArmor);
        return FlightCheck.ALLOWED;
    }

    /**
     * May this stack be used as a jetpack by this player right now, and which jetpack is it?
     * <p>
     * Every branch that refuses also tells the player why, so the caller only has to act on the
     * returned check.
     */
    private @NotNull Precondition preconditions(@Nullable ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) return Precondition.refused(FlightCheck.NOT_A_JETPACK);

        val jetpack = configs.jetpacks().get(itemTags.getJetpackID(stack, ""));
        if (jetpack == null) return Precondition.refused(FlightCheck.NOT_A_JETPACK);
        if (stack.getType() != jetpack.getItem()) return Precondition.refused(FlightCheck.NOT_A_JETPACK);
        if (this.jetpack != null && isActive() && !jetpack.getId().equals(this.jetpack.getId()))
            return Precondition.refused(FlightCheck.NOT_A_JETPACK);

        if (!Permissions.hasRawPermission(player, jetpack.getPermission()))
            return Precondition.refused(FlightCheck.NO_PERMISSION);

        val isWorldBlocked = jetpack.getBlockedWorlds().stream()
                .anyMatch(world -> player.getWorld().getName().equals(world));
        if (isWorldBlocked) {
            messages.send(player, configs.message().getBlockedWorlds());
            return Precondition.refused(FlightCheck.BLOCKED);
        }

        if (!protection.allowsSuperiorSkyblock(player, jetpack)) return Precondition.refused(FlightCheck.BLOCKED);
        if (!protection.allowsGriefPrevention(player, jetpack)) return Precondition.refused(FlightCheck.BLOCKED);

        return Precondition.allowed(jetpack);
    }

    /**
     * Charge one tick and decide whether the flight continues.
     *
     * @return {@link FlightCheck#ALLOWED} to keep flying, otherwise the refusal reason
     */
    @Contract(pure = true)
    private @NotNull FlightCheck burnAction() {
        assert jetpack != null;

        if (!Permissions.hasRawPermission(player, jetpack.getPermission()))
            return FlightCheck.NO_PERMISSION;

        if (!starting && ((LivingEntity) player).isOnGround() && !player.isFlying()) return FlightCheck.ALLOWED;

        if (jetpack != null && jetpack.isRunInOffHandOnly()) {
            val offHandItem = player.getInventory().getItemInOffHand();
            val check = isJetpack(offHandItem, !starting);
            if (check == FlightCheck.NO_PERMISSION || check == FlightCheck.BLOCKED) return check;
            if (check == FlightCheck.NOT_A_JETPACK) {
                turnOff();
                messages.send(player, configs.message().getDetached());
            }
            return FlightCheck.ALLOWED;
        }

        val equipment = player.getEquipment();
        assert equipment != null;
        for (@Nullable ItemStack armor : equipment.getArmorContents()) {
            val check = isJetpack(armor, !starting);
            if (check == FlightCheck.ALLOWED) return FlightCheck.ALLOWED;
            if (check != FlightCheck.NOT_A_JETPACK) return check;
        }

        turnOffDetached();
        return FlightCheck.ALLOWED;
    }

    /**
     * require {@link this#setJetpack(Jetpack)} frist!
     */
    public void turnOn() {
        if (starting) return;
        starting = true;

        assert jetpack != null;

        val delayInSec = jetpack.getFuel().getBurnRate();

        // main thread: the tick touches inventory/equipment/player state
        Bukkit.getScheduler().runTaskTimer(plugin, bukkitTask -> {
            if (burnTask == null) burnTask = bukkitTask;
            if (!starting && !isActive()) {
                bukkitTask.cancel();
                return;
            }
            Catcher.create(this::burnAction).onSuccess(check -> {
                if (check == FlightCheck.NO_PERMISSION) {
                    messages.send(player, configs.message().getNoPermission());
                    turnOff();
                    return;
                }
                if (check == FlightCheck.BLOCKED) {
                    turnOff();
                    return;
                }
                if (!starting) return;
                setActive(true);
                player.setAllowFlight(true);
                player.setFlySpeed(jetpack.getSpeed() / 10.0f);
                messages.send(player, configs.message().getTurnOn());

                particles.start(this);
            }).onFailure(error -> {
                turnOff(true);
                log.debug("Error: %s", error);
            }).onCompleted(() -> {
                if (starting)
                    starting = false;
            });
        }, 0L, delayInSec * 20L);

    }


    public void turnOffDetached() {
        messages.send(player, player.isFlying()
                        ? configs.message().getDetached()
                        : configs.message().getTurnOff()
        );
        turnOff();
    }

    public void turnOff() {
        turnOff(false);
    }

    /**
     * @param sendMsg report the turn off to the player
     */
    public void turnOff(boolean sendMsg) {
        if (jetpack == null || !isActive()) return;
        releaseFlight();

        if (sendMsg)
            messages.send(player, configs.message().getTurnOff());
        jetpack = null;
    }

    /** Plugin unload: the jetpack stays equipped on the player so it survives the restart. */
    public void turnOffUnloaded() {
        if (jetpack == null || !isActive()) return;
        releaseFlight();
        messages.send(player, "&cThis plugin has been unloaded!");
    }

    private void releaseFlight() {
        starting = false;
        setActive(false);
        player.setAllowFlight(false);

        if (burnTask != null)
            burnTask.cancel();
        burnTask = null;
        particles.stop();
        registry.remove(player);
    }

    /** Outcome of {@link #preconditions}: either a refusal, or the jetpack the stack turned out to be. */
    private record Precondition(@NotNull FlightCheck check, @Nullable Jetpack jetpack) {

        static @NotNull Precondition refused(@NotNull FlightCheck check) {
            return new Precondition(check, null);
        }

        static @NotNull Precondition allowed(@NotNull Jetpack jetpack) {
            return new Precondition(FlightCheck.ALLOWED, jetpack);
        }

    }

}
