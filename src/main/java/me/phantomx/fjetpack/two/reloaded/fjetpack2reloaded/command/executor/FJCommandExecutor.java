package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.executor;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.FJConfig;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Placeholder;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc.FJVersion;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface FJCommandExecutor {

    boolean isSupported(FJ2RCommand cmd);

    void onCommand(CommandSender sender, FJ2RCommand cmd, String[] args);


    default void addItemToInventory(FJConfig config, Player player, ItemStack storeItem) {
        var items = player.getInventory().addItem(storeItem);
        // drop if inventory is full!
        for (ItemStack item : items.values()) {
            var location = player.getLocation();
            player.getWorld().dropItemNaturally(location, item);

            var msg = config.message().getInventoryFull()
                    .replace(Placeholder.X, String.valueOf(location.getX()))
                    .replace(Placeholder.Y, String.valueOf(location.getY()))
                    .replace(Placeholder.Z, String.valueOf(location.getZ()));
            Messages.sendMessage(player, msg);
        }
        player.updateInventory();
    }

    default ItemStack getItemInHand(FJVersion version, Player player) {
        @SuppressWarnings("deprecation")
        val item = version.getServerVersion() > 8 ? player.getInventory().getItemInMainHand() : player.getItemInHand();
        val meta = item.getItemMeta();
        if (meta == null || item.getType() == Material.AIR) {
            Messages.sendMessage(player, "&cYou not holding any item in hand.");
            return null;
        }
        return item;
    }
}
