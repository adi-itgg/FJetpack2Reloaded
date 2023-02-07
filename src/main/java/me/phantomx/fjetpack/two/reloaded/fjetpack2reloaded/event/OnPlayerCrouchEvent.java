package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.event;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.Configs;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.FJ2RPlayer;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.logging.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemMetaData;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Permissions;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OnPlayerCrouchEvent {

    private static final Log log = new Log("OnPlayerCrouchEvent");

    public static void onToggleSneak(@NotNull PlayerToggleSneakEvent e) {
        val player = e.getPlayer();
        if (e.isSneaking() || !((LivingEntity) player).isOnGround()) return;

        val ac = FJ2RPlayer.getAsFJ2RPlayer(player);
        if (ac.isActive()) {
            ac.turnOff(true);
            return;
        }

        val equipment = player.getEquipment();
        assert equipment != null;
        for (@Nullable ItemStack armor : equipment.getArmorContents()) {
            if (armor == null || armor.getType() == Material.AIR) continue;

            val jetpackId = ItemMetaData.getJetpackID(armor, "");
            val jetpack = Configs.getJetpacksLoaded().get(jetpackId);
            if (jetpack == null) continue;

            if (!Permissions.hasRawPermission(player, jetpack.getPermission()))
                NoPermissionLvlException.send();

            ac.turnOn(jetpack);
            return;
        }

    }



}
