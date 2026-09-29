package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import lombok.*;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.JetpackEvent;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.IMessageException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.ActionResultReturnHandler;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.hook.GriefPrevention;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.hook.SuperiorSkyblock;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemTagCodec;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.logging.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Placeholder;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Permissions;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Version;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Particle;
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

import static java.lang.Math.sin;

@Data
public class FJ2RPlayer {

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
    private final Log log;

    private boolean isActive;
    private @NonNull Player player;
    @Setter(AccessLevel.PRIVATE)
    private @Nullable Jetpack jetpack;
    @Setter(AccessLevel.PRIVATE)
    private @Nullable BukkitTask burnTask;
    @Setter(AccessLevel.PRIVATE)
    private @Nullable BukkitTask particleTask;
    private @Nullable UUID superiorSkyblock2IslandUUID;
    private @Nullable Claim griefPreventionClaim;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean starting;
    @Setter(AccessLevel.NONE)
    private int jetpackUniqueId;

    public FJ2RPlayer(@NotNull Player player,
                      @NotNull PlayerRegistry registry,
                      @NotNull JavaPlugin plugin,
                      @NotNull YamlConfigRepository configs,
                      @NotNull MessageSink messages,
                      @NotNull ItemTagCodec itemTags) {
        this.player = player;
        this.registry = registry;
        this.plugin = plugin;
        this.configs = configs;
        this.messages = messages;
        this.itemTags = itemTags;
        this.log = new Log(getClass(), messages);
    }

    public boolean isAllowedGriefPrevention() {
        if (jetpack == null) return true;
        return isAllowedGriefPrevention(jetpack);
    }

    private boolean isAllowedGriefPrevention(@NotNull Jetpack jetpack) {
        if (Permissions.isAdminOrOp(player)) return true;
        val griefPrevention = jetpack.getGriefPrevention();
        if (griefPrevention == null || !griefPrevention.isEnable() || !GriefPrevention.isActive()) return true;
        @Nullable val claim = GriefPrevention.get().dataStore.getClaimAt(player.getLocation(),
                true,
                true,
                getGriefPreventionClaim()
        );
        if (griefPrevention.isAllowInsideAllClaim()) {
            if (claim != null) {
                setGriefPreventionClaim(claim);
                return true;
            }
            messages.send(player, configs.message().getGriefPreventionOutsideClaim());
            return false;
        }
        if (claim == null) {
            messages.send(player, configs.message().getGriefPreventionOutsideClaim());
            return false;
        }
        if (griefPrevention.isAllowBypassClaim() && Permissions.hasRawPermission(player,
                jetpack.getPermission(Permissions.PERMISSION_BYPASS_GRIEF_PREVENTION_CLAIM))
        ) return true;
        if (!griefPrevention.isOnlyAllowInsideOwnClaim()) {
            messages.send(player, configs.message().getNoPermission());
            return false;
        }
        if (claim.getOwnerID() != player.getUniqueId()) {
            messages.send(player, configs.message().getNoPermission());
            return false;
        }
        setGriefPreventionClaim(claim);
        return true;
    }

    public boolean isAllowedSuperiorSkyblock() {
        if (jetpack == null) return true;
        return isAllowedSuperiorSkyblock(jetpack);
    }

    private boolean isAllowedSuperiorSkyblock(@NotNull Jetpack jetpack) {
        if (!SuperiorSkyblock.isActive() || Permissions.isAdminOrOp(player)) return true;
        val superiorSkyblock2 = jetpack.getSuperiorSkyblock2();
        if (superiorSkyblock2 == null || !superiorSkyblock2.isEnable()) return true;
        // flag check
        if (superiorSkyblock2.isAllowBypassFlag() && Permissions.hasRawPermission(player,
                jetpack.getPermission(Permissions.PERMISSION_BYPASS_SUPERIOR_SKYBLOCK_FLAG)
        )) return true;
        val island = SuperiorSkyblockAPI.getIslandAt(player.getLocation());
        if (island == null) {
            messages.send(player, configs.message().getSuperiorSkyblock2OutsideIsland());
            return true;
        }
        if (!island.hasSettingsEnabled(IslandFlag.getByName(SuperiorSkyblock.FLAG_PRIVILEGE))) {
            messages.send(player, configs.message().getSuperiorSkyblock2NoFlag());
            return false;
        }
        // privilege check
        if (superiorSkyblock2.isAllowBypassPrivilege() && Permissions.hasRawPermission(player,
                jetpack.getPermission(Permissions.PERMISSION_BYPASS_SUPERIOR_SKYBLOCK_PRIVILEGE)
        )) return true;
        val hasPerm = island.hasPermission(player, IslandPrivilege.getByName(SuperiorSkyblock.FLAG_PRIVILEGE));
        if (!hasPerm)
            messages.send(player, configs.message().getSuperiorSkyblock2NoPermission());
        if (hasPerm)
            setSuperiorSkyblock2IslandUUID(island.getUniqueId());
        return hasPerm;
    }

    private void particleAction(Object obj) {
        if (jetpack == null || jetpack.getParticle() == null) return;
        if (!isActive() || !player.isFlying() || ((LivingEntity) player).isOnGround()) return;

        val location = player.getLocation();
        if (Version.getServerVersion() <= 8 && obj instanceof Effect effect) {
            player.getWorld().playEffect(
                    location.add(0.0, 0.8, 0.0),
                    effect,
                    0
            );
            return;
        }
        if (!(obj instanceof Particle particle)) return;
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

    private void startParticle() {
        // start particle
        log.log("starting particle effect");
        if (jetpack == null || jetpack.getParticle() == null || !jetpack.getParticle().isEnable()) return;
        val effectName = jetpack.getParticle().getEffect().toUpperCase().trim();
        val effect = Catcher.create(() ->
                Version.getServerVersion() > 8 ? Particle.valueOf(effectName) : Effect.valueOf(effectName)
        ).onFailure(error -> {
            log.log("&cInvalid particle effect name: %s - details: %s", jetpack.getParticle().getEffect(), error);
        }).getOrDefault(Version.getServerVersion() > 8 ? Particle.CLOUD : Effect.SMOKE);
        log.log("creating task for particle effect");
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task -> {
            if (particleTask == null) particleTask = task;
            if (!isActive()) {
                task.cancel();
                return;
            }
            particleAction(effect);
            }, 0L, jetpack.getParticle().getDelay()
        );
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
        if (jetpack == null || jetpack.getOnEmptyFuel() == JetpackEvent.NONE) return;
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

    public boolean isJetpack(@Nullable ItemStack stack, boolean consumeFuel) {
        return isJetpack(stack, consumeFuel, false);
    }

    public boolean isJetpack(@Nullable ItemStack stack, boolean consumeFuel, boolean updateCurrentJetpack) {
        if (stack == null || stack.getType() == Material.AIR) return false;

        val jetpackId = itemTags.getJetpackID(stack, "");
        val jetpack = configs.jetpacks().get(jetpackId);
        if (jetpack == null) return false;

        if (stack.getType() != jetpack.getItem()) return false;
        if (this.jetpack != null && isActive() && !jetpack.getId().equals(this.jetpack.getId())) return false;

        if (!Permissions.hasRawPermission(player, jetpack.getPermission()))
            NoPermissionLvlException.send();

        val isWorldBlocked = jetpack.getBlockedWorlds().stream()
                .anyMatch(world -> player.getWorld().getName().equals(world));
        if (isWorldBlocked) {
            messages.send(player, configs.message().getBlockedWorlds());
            IMessageException.send();
        }

        if (!isAllowedSuperiorSkyblock())
            IMessageException.send();

        if (!isAllowedGriefPrevention())
            IMessageException.send();


        var fuel = itemTags.getFuelValue(stack);
        assert fuel != null;
        val canBypassCost = Permissions.hasRawPermission(player,
                jetpack.getPermission() + Permissions.PERMISSION_BYPASS_FUEL_SUFFIX
        );
        if (!canBypassCost && fuel < jetpack.getFuel().getCost()) {
            ItemFactory.updateDisplayItem(jetpack, stack, fuel);
            onOutOfFuel();
            IMessageException.send();
        }
        if (!canBypassCost)
            fuel -= jetpack.getFuel().getCost();

        val canBypassSprintCost = Permissions.hasRawPermission(player,
                jetpack.getPermission() + Permissions.PERMISSION_BYPASS_FUEL_SPRINT_SUFFIX
        );
        if (!canBypassSprintCost && player.isSprinting() && fuel < jetpack.getFuel().getSprintCost()) {
            ItemFactory.updateDisplayItem(jetpack, stack, fuel);
            onOutOfFuel();
            IMessageException.send();
        }
        if (!canBypassSprintCost && player.isSprinting())
            fuel -= jetpack.getFuel().getSprintCost();

        if (updateCurrentJetpack)
            setJetpack(jetpack);

        if (!consumeFuel) {
            jetpackUniqueId = itemTags.newItemId();
            stack = itemTags.setActiveJetpack(stack, jetpackUniqueId);
            @NotNull ItemStack finalArmor = stack;
            updateActiveJetpackArmorEquipment(item -> finalArmor);
            return true;
        }

        if (jetpack.getFuel().getWarnRunOutBelow() != -1 && fuel <= jetpack.getFuel().getWarnRunOutBelow())
            messages.send(player,
                    configs.message().getWarnRunOutBelow().replace(Placeholder.AMOUNT, String.valueOf(fuel))
            );

        ItemFactory.updateDisplayItem(jetpack, stack, fuel);
        stack = itemTags.setFuelValue(stack, fuel);
        @NotNull ItemStack finalArmor = stack;
        updateActiveJetpackArmorEquipment(item -> finalArmor);
        return true;
    }

    @Contract(pure = true)
    private void burnAction() {
        assert jetpack != null;

        if (!Permissions.hasRawPermission(player, jetpack.getPermission()))
            NoPermissionLvlException.send();

        if (!starting && ((LivingEntity) player).isOnGround() && !player.isFlying()) return;

        if (jetpack != null && jetpack.isRunInOffHandOnly()) {
            val offHandItem = player.getInventory().getItemInOffHand();
            if (!isJetpack(offHandItem, !starting)) {
                turnOff();
                messages.send(player, configs.message().getDetached());
            }
            return;
        }

        val equipment = player.getEquipment();
        assert equipment != null;
        for (@Nullable ItemStack armor : equipment.getArmorContents())
            if (isJetpack(armor, !starting)) return;

        turnOffDetached();
    }

    /**
     * require {@link this#setJetpack(Jetpack)} frist!
     */
    public void turnOn() {
        if (starting) return;
        starting = true;

        assert jetpack != null;

        val delayInSec = jetpack.getFuel().getBurnRate();

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, bukkitTask -> {
            if (burnTask == null) burnTask = bukkitTask;
            if (!starting && !isActive()) {
                bukkitTask.cancel();
                return;
            }
            Catcher.createVoid(this::burnAction).onSuccess(nothing -> {
                if (!starting) return;
                setActive(true);
                player.setAllowFlight(true);
                player.setFlySpeed(jetpack.getSpeed() / 10.0f);
                messages.send(player, configs.message().getTurnOn());

                startParticle();
            }).onFailure(error -> {
                if (error instanceof IMessageException) {
                    turnOff();
                    return;
                }
                if (error instanceof NoPermissionLvlException) {
                    messages.send(player, configs.message().getNoPermission());
                    turnOff();
                    return;
                }
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
        if (particleTask != null)
            particleTask.cancel();
        particleTask = null;
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
