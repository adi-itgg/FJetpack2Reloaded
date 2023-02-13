package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.event;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.Configs;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.FJ2RPlayer;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemMetaData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OnPlayerCrouchEvent {

    public static void onToggleSneak(@NotNull PlayerToggleSneakEvent e) {
        val player = e.getPlayer();
        if (e.isSneaking() || !((LivingEntity) player).isOnGround()) return;

        val ac = FJ2RPlayer.getAsFJ2RPlayer(player);
        if (ac.isActive()) {
            ac.turnOff(true);
            return;
        }

        val offHandItem = player.getInventory().getItemInOffHand();
        var jetpackId = ItemMetaData.getJetpackID(offHandItem, "");
        var jetpack = Configs.getJetpacksLoaded().get(jetpackId);
        if (jetpack != null && jetpack.isRunInOffHandOnly()) {
            if (ac.isJetpack(offHandItem, false, true))
                ac.turnOn();
            return;
        }

        val equipment = player.getEquipment();
        assert equipment != null;
        for (@Nullable ItemStack armor : equipment.getArmorContents()) {
            if (!ac.isJetpack(armor, false, true)) continue;
            if (ac.getJetpack() == null) continue;
            if (ac.getJetpack().isRunInOffHandOnly()) return;
            ac.turnOn();
            return;
        }

    }



}
