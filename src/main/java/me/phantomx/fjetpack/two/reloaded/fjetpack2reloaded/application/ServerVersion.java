package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application;

import jakarta.inject.Singleton;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Facts about the server this plugin runs on.
 * <p>
 * An injected bean rather than static state: both values are derived from a live server, so a static
 * initializer would run class-init at an arbitrary moment and a unit test could never choose them.
 * Each is read once, when the bean is built.
 */
@Singleton
public class ServerVersion {

    private final int serverVersion;
    private final @NotNull String nmsApiVersion;

    public ServerVersion() {
        this.serverVersion = parseServerVersion();
        this.nmsApiVersion = parseNmsApiVersion();
    }

    public int getServerVersion() {
        return serverVersion;
    }

    public @NotNull String getNmsApiVersion() {
        return nmsApiVersion;
    }

    private static int parseServerVersion() {
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
    }

    private static @NotNull String parseNmsApiVersion() {
        val sp = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        if (sp.length > 3) {
            return sp[3];
        }
        return Bukkit.getServer().getVersion();
    }

}
