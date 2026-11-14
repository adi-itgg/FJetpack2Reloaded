package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook;

import lombok.val;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class SuperiorSkyblock {

    public static final String PLUGIN_NAME = "SuperiorSkyblock2";
    public static final String FLAG_PRIVILEGE = "FJETPACK2_RELOADED";

    public static boolean isActive() {
        val plugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
        if (plugin == null) return false;
        return plugin.isEnabled();
    }

    /**
     * Whether a class of the SuperiorSkyblock2 API can be loaded on this server.
     * <p>
     * The API is a {@code compileOnly} dependency: without the plugin its event classes do not
     * exist, and Bukkit only finds out while it scans a listener's handler methods - then it logs a
     * SEVERE "Failed to register events" for that listener. The optional listeners ask this first,
     * so an uninstalled plugin costs an INFO line instead of an ERROR.
     * <p>
     * A class check, not {@link #isActive()}: the plugin is {@code loadbefore}d, so at our own
     * enable it is loaded but not enabled yet - its classes are visible, its events simply have not
     * fired.
     */
    public static boolean apiClassPresent(@NotNull String className) {
        try {
            Class.forName(className, false, SuperiorSkyblock.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError absent) {
            return false;
        }
    }

}
