package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.item.ItemTagCodec;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Server environment helpers (pure: derived once from Bukkit, never mutated).
 */
public class Version {

    @Getter(lazy = true)
    private static final int serverVersion = ((Supplier<Integer>) () -> {
        val pattern = Pattern.compile("(?<= )([\\d.]+)(?=\\))");
        val matcher = pattern.matcher(Bukkit.getVersion());
        if (matcher.find()) {
            val version = NumberUtils.toInt(matcher.group(1).split("\\.")[1], Integer.MIN_VALUE);
            if (version != Integer.MIN_VALUE) {
                return version;
            }
        }
        val versionString = Bukkit.getVersion().split("-")[0];
        if (versionString.contains(".")) {
            val version = NumberUtils.toInt(versionString.split("\\.")[1], Integer.MIN_VALUE);
            if (version != Integer.MIN_VALUE) {
                return version;
            }
        }
        return NumberUtils.toInt(versionString, 0);
    }).get();

    @Getter(lazy = true)
    private static final @NonNull String nmsApiVersion = ((Supplier<String>) () -> {
        val sp = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        if (sp.length > 3) {
            return sp[3];
        }
        return Bukkit.getServer().getVersion();
    }).get();

    public static boolean isServerSupport(@NotNull MessageSink messages, @NotNull ItemTagCodec itemTags) {
        messages.sendToConsole("&6Checking Server Version...");
        if (getServerVersion() == 0) {
            messages.sendToConsole("&cUnknown Server Version! - " + Bukkit.getVersion());
            return false;
        }
        if (getServerVersion() > 17) {
            var armor = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
            armor = itemTags.setJetpack(armor, getNmsApiVersion());
            val storedValue = itemTags.getJetpackID(armor, "");
            if (!storedValue.equals(getNmsApiVersion())) {
                messages.sendToConsole("&cUnsupported server api version!");
                return false;
            }
        }
        messages.sendToConsole("&6Detected Server: &a%s v%d - API %s", Bukkit.getName(), getServerVersion(), getNmsApiVersion());
        return true;
    }

}
