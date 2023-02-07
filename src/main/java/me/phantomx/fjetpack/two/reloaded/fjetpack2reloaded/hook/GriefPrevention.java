package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.hook;

import lombok.val;
import org.bukkit.Bukkit;

public class GriefPrevention {

    public static final String PLUGIN_NAME = "GriefPrevention";

    public static me.ryanhamshire.GriefPrevention.GriefPrevention get() {
        return me.ryanhamshire.GriefPrevention.GriefPrevention.instance;
    }

    public static boolean isActive() {
        val plugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
        if (plugin == null) return false;
        return plugin.isEnabled();
    }
}
