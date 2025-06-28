package me.phantomx.fjetpack.two.reloaded.v2;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.v2.config.ConfigManager;
import me.phantomx.fjetpack.two.reloaded.v2.hook.HookManager;
import me.phantomx.fjetpack.two.reloaded.v2.util.VersionManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class FJReloaded extends JavaPlugin {

    private static final int metricsId = 17668;

    @Override
    public void onEnable() {
        // version manager
        val versionManager = new VersionManager(getServer(), getLogger());

        // check version
        if (!versionManager.isServerSupport()) {
            setEnabled(false);
            return;
        }

        // hooks
        val hookManager = new HookManager(getServer().getPluginManager());
        hookManager.registerHooks();

        // config
        val configManager = new ConfigManager(this);
        configManager.loadAllConfig(getServer().getConsoleSender());

        // metrics
        new Metrics(this, metricsId);
    }




}
