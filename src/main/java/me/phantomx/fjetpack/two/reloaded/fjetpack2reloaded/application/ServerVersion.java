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
        this.serverVersion = parseServerVersion(Bukkit.getVersion());
        this.nmsApiVersion = parseNmsApiVersion(Bukkit.getServer().getClass().getPackage().getName());
    }

    public int getServerVersion() {
        return serverVersion;
    }

    public @NotNull String getNmsApiVersion() {
        return nmsApiVersion;
    }

    /**
     * The 1.x minor version (1.19.3 -&gt; 19) and, from 2.0 on, {@code major * 100 + minor}
     * (MC 26.3 -&gt; 2603) so the new scheme compares above every 1.x gate. 0 when unreadable.
     * <p>
     * Package-private and string-taking so a test does not need a live server.
     */
    static int parseServerVersion(@NotNull String rawVersion) {
        val matcher = Pattern.compile("(\\d+)\\.(\\d+)").matcher(rawVersion);
        if (!matcher.find()) return 0;

        val major = NumberUtils.toInt(matcher.group(1), 0);
        val minor = NumberUtils.toInt(matcher.group(2), 0);
        // ponytail: any MC >= 2 encodes as major*100+minor; enough for the "> N" gates.
        // Swap for isAtLeast(major, minor) if a gate ever needs a real 2.x boundary.
        return major == 1 ? minor : major * 100 + minor;
    }

    /**
     * The CraftBukkit package segment ("v1_19_R3"), or "" when the package is flattened - which is
     * every server from 1.20.5 on. Package-private and string-taking so a test needs no server.
     */
    static @NotNull String parseNmsApiVersion(@NotNull String serverPackage) {
        val sp = serverPackage.split("\\.");
        return sp.length > 3 ? sp[3] : "";
    }

}
