package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.hook;

import lombok.val;
import org.bukkit.Bukkit;

public class SuperiorSkyblock {

    public static final String PLUGIN_NAME = "SuperiorSkyblock2";
    public static final String FLAG_PRIVILEGE = "FJETPACK2_RELOADED";

    public static boolean isActive() {
        val plugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
        if (plugin == null) return false;
        return plugin.isEnabled();
    }

}
