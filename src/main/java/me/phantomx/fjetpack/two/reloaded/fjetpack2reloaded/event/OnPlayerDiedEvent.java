package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.event;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.Configs;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.FJ2RPlayer;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.JetpackEvent;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Permissions;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OnPlayerDiedEvent {


    public static void onDied(@NotNull PlayerDeathEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) return;

        val player = (Player) e.getEntity();
        val fj2RPlayer = FJ2RPlayer.getAsFJ2RPlayer(player);
        if (!fj2RPlayer.isActive()) return;
        if (fj2RPlayer.getJetpack() == null || fj2RPlayer.getJetpack().getOnDeath() == JetpackEvent.NONE) return;
        if (Permissions.hasRawPermission(player, fj2RPlayer.getJetpack().getPermission(Permissions.PERMISSION_KEEP_ON_DEATH_SUFFIX))) return;

        fj2RPlayer.turnOff();

        if (fj2RPlayer.getJetpack().isRunInOffHandOnly()) {
            val offHandItem = player.getInventory().getItemInOffHand();
            onDeathEvent(offHandItem, player, fj2RPlayer.getJetpack());
            return;
        }

        val equipment = player.getEquipment();
        if (equipment == null) return;

        fj2RPlayer.updateActiveJetpackArmorEquipment(item -> onDeathEvent(item, player, fj2RPlayer.getJetpack()));
    }

    private static @Nullable ItemStack onDeathEvent(@NotNull ItemStack item, @NotNull Player player, @NotNull Jetpack jetpack) {
        return switch (jetpack.getOnDeath()) {
            case NONE -> item;
            case REMOVE -> {
                Messages.sendMessage(player, Configs.getMessage().getOnDeathRemoved());
                yield null;
            }
            case DROP -> {
                player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
                Messages.sendMessage(player, Configs.getMessage().getOnDeathDropped());
                yield null;
            }
        };
    }


}
