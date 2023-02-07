package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemMetaData;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerUtil {

    public static @Nullable ItemStack getItemInHand(@NotNull Player player) {
        @SuppressWarnings("deprecation")
        val item = Version.getServerVersion() > 8 ? player.getInventory().getItemInMainHand() : player.getItemInHand();
        val meta = item.getItemMeta();
        if (meta == null || item.getType() == Material.AIR) {
            Messages.sendMessage(player, "&cYou not holding any item in hand.");
            return null;
        }
        if (ItemMetaData.isNotItemArmor(item)) {
            Messages.sendMessage(player, "&cThis item is not armor item!");
            return null;
        }
        return item;
    }


}
