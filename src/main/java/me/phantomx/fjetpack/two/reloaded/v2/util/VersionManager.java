package me.phantomx.fjetpack.two.reloaded.v2.util;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemMetaData;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;
import java.util.regex.Pattern;

@Getter
@Accessors(fluent = true)
public final class VersionManager {

    private final Server server;
    private final Logger logger;

    private final int serverVersion;
    private final String nmsApiVersion;

    public VersionManager(@NotNull Server server, Logger logger) {
        this.server = server;
        this.logger = logger;

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


    public boolean isServerSupport() {
        logger.info(MessageUtil.translateColorCodes("&6Checking Server Version..."));
        if (serverVersion == 0) {
            logger.warning(MessageUtil.translateColorCodes("&cUnknown Server Version! - " + server.getVersion()));
            return false;
        }
        if (serverVersion > 17) {
            var armor = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
            armor = ItemMetaData.setJetpack(armor, nmsApiVersion);
            val storedValue = ItemMetaData.getJetpackID(armor, "");
            if (!storedValue.equals(nmsApiVersion)) {
                logger.warning(MessageUtil.translateColorCodes("&cUnsupported server api version!"));
                return false;
            }
        }
        logger.info(MessageUtil.translateColorCodes("&6Detected Server: &a" + server.getName() + " v" + serverVersion + " - API " + nmsApiVersion));
        return true;
    }

}
