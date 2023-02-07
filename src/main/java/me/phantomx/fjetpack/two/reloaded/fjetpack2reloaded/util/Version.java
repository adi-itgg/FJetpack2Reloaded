package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util;

import lombok.*;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.FJetpack2Reloaded;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemMetaData;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Scanner;


public class Version {

    private static final int resourceId = 107883;

    @Getter(lazy = true)
    private static final int serverVersion = NumberUtils.toInt(Bukkit.getVersion().split("MC:")[1].split("\\.")[1], 0);
    @Getter(lazy = true)
    private static final @NonNull String nmsApiVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    @Getter(lazy = true)
    private static final boolean isServerSupport = _isServerSupport();

    private static boolean _isServerSupport() {
        if (getServerVersion() == 0) {
            Messages.sendMessage("&cUnknown Server Version!");
            return false;
        }
        if (getServerVersion() > 17) {
            var armor = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
            armor = ItemMetaData.setJetpack(armor, getNmsApiVersion());
            val storedValue = ItemMetaData.getJetpackID(armor, "");
            if (!storedValue.equals(getNmsApiVersion())) {
                Messages.sendMessage("&cUnsupported server api version!");
                return false;
            }
        }
        Messages.sendMessage("&6Detected Server: &a%s v%d - API %s", Bukkit.getName(), getServerVersion(), getNmsApiVersion());
        return true;
    }

    public static void checkUpdate(@NotNull CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(FJetpack2Reloaded.getPlugin(), () -> {
            Catcher.createVoid(() -> {
                @Cleanup val inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId).openStream();
                @Cleanup val scanner = new Scanner(inputStream);
                while (scanner.hasNext()) {
                    val spigotVersion = scanner.next();
                    val pluginVersion = Integer.parseInt(
                            FJetpack2Reloaded.getPlugin()
                                    .getDescription()
                                    .getVersion()
                                    .replaceAll("\\D+", "")
                    );

                    if (pluginVersion < Integer.parseInt(spigotVersion.replaceAll("\\D+", ""))) {
                        Messages.sendMessage(sender ,"&bThere is a new update available! v%s\nhttps://www.spigotmc.org/resources/fjetpack2reloaded.107883/", spigotVersion);
                        return;
                    }
                    Messages.sendMessage(sender, "&aThere is not a new update available. You are using the latest version");
                }
            }).onFailure(error -> Messages.sendMessage(sender, "Unable to check for updates: %s", error));
        });
    }

}
