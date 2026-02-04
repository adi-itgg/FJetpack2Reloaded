package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginMetrics extends Metrics {

    private static final int metricsId = 17668;

    public PluginMetrics(JavaPlugin plugin) {
        super(plugin, metricsId);
    }

}
