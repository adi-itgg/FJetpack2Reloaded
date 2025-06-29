package me.phantomx.fjetpack.two.reloaded.v2.util;

import lombok.Cleanup;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.v2.item.ItemMeta;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Getter
@Accessors(fluent = true)
public final class VersionManager {

    private static final int resourceId = 107883;

    private final Server server;
    private final Logger logger;
    private final Plugin plugin;

    private final int serverVersion;
    private final String nmsApiVersion;

    public VersionManager(@NotNull Server server, Logger logger, Plugin plugin) {
        this.server = server;
        this.logger = logger;
        this.plugin = plugin;

        this.serverVersion = getServerVersion();
        this.nmsApiVersion = getNmsApiVersion();
    }

    private int getServerVersion() {
        val pattern = Pattern.compile("(?<= )([\\d.]+)(?=\\))"); // git-Paper-448 (MC: 1.19.3)
        val matcher = pattern.matcher(server.getVersion());
        if (matcher.find()) {
            val version = Utils.toInt(matcher.group(1).split("\\.")[1], Integer.MIN_VALUE);
            if (version != Integer.MIN_VALUE) {
                return version;
            }
        }
        val versionString = server.getVersion().split("-")[0];
        if (versionString.contains(".")) {
            val version = Utils.toInt(versionString.split("\\.")[1], Integer.MIN_VALUE);
            if (version != Integer.MIN_VALUE) {
                return version;
            }
        }
        return Utils.toInt(versionString, 0);
    }

    private String getNmsApiVersion() {
        val sp = server.getClass().getPackage().getName().split("\\.");
        if (sp.length > 3) {
            return sp[3];
        }
        return server.getVersion();
    }


    public boolean isServerSupport(ItemMeta itemMeta) {
        logger.info(MessageUtil.translateColorCodes("&6Checking Server Version..."));
        if (serverVersion == 0) {
            logger.warning(MessageUtil.translateColorCodes("&cUnknown Server Version! - " + server.getVersion()));
            return false;
        }
        if (serverVersion > 17) {
            var armor = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
            armor = itemMeta.setJetpack(armor, nmsApiVersion);
            val storedValue = itemMeta.getJetpackID(armor, "");
            if (!storedValue.equals(nmsApiVersion)) {
                logger.warning(MessageUtil.translateColorCodes("&cUnsupported server api version!"));
                return false;
            }
        }
        logger.info(MessageUtil.translateColorCodes("&6Detected Server: &a" + server.getName() + " v" + serverVersion + " - API " + nmsApiVersion));
        return true;
    }

    public void checkUpdate(@NotNull CommandSender sender) {
        server.getScheduler().runTaskAsynchronously(plugin, () -> checkUpdateSpigot(sender));
    }

    private void checkUpdateSpigot(@NotNull CommandSender sender) {
        try {
            MessageUtil.sendMessage(sender, "&6Checking for updates...");
            @Cleanup val inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId).openStream();
            @Cleanup val scanner = new Scanner(inputStream);
            while (scanner.hasNext()) {
                val spigotVersion = Utils.toInt(scanner.next().replaceAll("\\D+", ""), -1);
                val pluginVersion = Utils.toInt(plugin.getDescription().getVersion().replaceAll("\\D+", ""), -1);

                if (pluginVersion < spigotVersion) {
                    MessageUtil.sendMessage(sender, "&bThere is a new update available! v" + spigotVersion + "\nhttps://www.spigotmc.org/resources/fjetpack2reloaded.107883/");
                    return;
                }
                MessageUtil.sendMessage(sender, "&aThere is not a new update available. You are using the latest version");
            }
        } catch (Throwable e) {
            MessageUtil.sendMessage(sender, "Unable to check for updates: " + e.getMessage());
        }
    }

}
