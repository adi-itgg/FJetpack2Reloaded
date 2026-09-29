package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application;

import lombok.*;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.JetpackParticles;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ProtectionGuard;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.dto.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight.FlightCheck;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.fuel.FuelPolicy;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.fuel.FuelStop;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight.ItemDisposition;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.ActionResultReturnHandler;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.item.ItemTagCodec;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.logging.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.Placeholder;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util.Permissions;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util.Version;
import me.ryanhamshire.GriefPrevention.Claim;
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
import java.util.UUID;

/**
 * Live state and flight lifecycle of one player.
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
    private final YamlConfigRepository configs;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final MessageSink messages;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final ItemTagCodec itemTags;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final ProtectionGuard protection;
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
    private @Nullable UUID superiorSkyblock2IslandUUID;
    private @Nullable Claim griefPreventionClaim;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean starting;
    @Setter(AccessLevel.NONE)
    private int jetpackUniqueId;

    public FlightSession(@NotNull Player player,
                      @NotNull PlayerRegistry registry,
                      @NotNull JavaPlugin plugin,
                      @NotNull YamlConfigRepository configs,
                      @NotNull MessageSink messages,
                      @NotNull ItemTagCodec itemTags,
                      @NotNull ProtectionGuard protection) {
        this.player = player;
        this.registry = registry;
        this.plugin = plugin;
        this.configs = configs;
        this.messages = messages;
        this.itemTags = itemTags;
        this.protection = protection;
        this.particles = new JetpackParticles(plugin, messages);
        this.log = new Log(getClass(), messages);
    }

    public boolean isAllowedGriefPrevention() {
        if (jetpack == null) return true;
        return protection.allowsGriefPrevention(this, jetpack);
    }

    public boolean isAllowedSuperiorSkyblock() {
        if (jetpack == null) return true;
        return protection.allowsSuperiorSkyblock(this, jetpack);
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
                if (Version.getServerVersion() > 16) {
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
        // NOTE: pre-existing behaviour - the outcome uses OnDeath although this is the out-of-fuel path.
        updateActiveJetpackArmorEquipment(item -> switch (jetpack.getOnDeath()) {
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
        if (stack == null || stack.getType() == Material.AIR) return FlightCheck.NOT_A_JETPACK;

        val jetpack = configs.jetpacks().get(itemTags.getJetpackID(stack, ""));
        if (jetpack == null) return FlightCheck.NOT_A_JETPACK;

        if (stack.getType() != jetpack.getItem()) return FlightCheck.NOT_A_JETPACK;
        if (this.jetpack != null && isActive() && !jetpack.getId().equals(this.jetpack.getId())) return FlightCheck.NOT_A_JETPACK;

        if (!Permissions.hasRawPermission(player, jetpack.getPermission())) return FlightCheck.NO_PERMISSION;

        val isWorldBlocked = jetpack.getBlockedWorlds().stream()
                .anyMatch(world -> player.getWorld().getName().equals(world));
        if (isWorldBlocked) {
            messages.send(player, configs.message().getBlockedWorlds());
            return FlightCheck.BLOCKED;
        }

        if (!protection.allowsSuperiorSkyblock(this, jetpack)) return FlightCheck.BLOCKED;
        if (!protection.allowsGriefPrevention(this, jetpack)) return FlightCheck.BLOCKED;

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
            ItemFactory.updateDisplayItem(jetpack, stack, settlement.fuel());
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

        ItemFactory.updateDisplayItem(jetpack, stack, fuel);
        stack = itemTags.setFuelValue(stack, fuel);
        @NotNull ItemStack finalArmor = stack;
        updateActiveJetpackArmorEquipment(item -> finalArmor);
        return FlightCheck.ALLOWED;
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
                turnOff(true, false, false, false);
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
        turnOff(false, false ,false, false);
    }

    public void turnOff(boolean sendMsg) {
        turnOff(sendMsg, false,false, false);
    }

    public void turnOff(boolean sendMsg, boolean onDied, boolean onEmptyFuel, boolean unloadedPlugin) {
        if (jetpack == null || !isActive()) return;
        starting = false;
        setActive(false);
        player.setAllowFlight(false);

        if (burnTask != null)
            burnTask.cancel();
        burnTask = null;
        particles.stop();
        registry.remove(player);
        griefPreventionClaim = null;
        superiorSkyblock2IslandUUID = null;

        if (onDied) {
            val msg = switch (jetpack.getOnDeath()) {
                case NONE -> null;
                case DROP -> configs.message().getOnDeathDropped();
                case REMOVE -> configs.message().getOnDeathRemoved();
            };
            if (msg != null)
                messages.send(player, msg);
            return;
        }
        if (onEmptyFuel) {
            val msg = switch (jetpack.getOnEmptyFuel()) {
                case NONE -> null;
                case DROP -> configs.message().getOnEmptyFuelDropped();
                case REMOVE -> configs.message().getOnEmptyFuelRemoved();
            };
            if (msg != null)
                messages.send(player, msg);
            return;
        }
        if (unloadedPlugin) {
            messages.send(player, "&cThis plugin has been unloaded!");
            return;
        }

        if (sendMsg)
            messages.send(player, configs.message().getTurnOff());
        jetpack = null;
    }

}
