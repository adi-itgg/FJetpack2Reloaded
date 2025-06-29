package me.phantomx.fjetpack.two.reloaded.v2;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.v2.config.ConfigManager;
import me.phantomx.fjetpack.two.reloaded.v2.hook.HookManager;
import me.phantomx.fjetpack.two.reloaded.v2.item.ItemMeta;
import me.phantomx.fjetpack.two.reloaded.v2.util.Utils;
import me.phantomx.fjetpack.two.reloaded.v2.util.VersionManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
@Accessors(fluent = true)
public class FJReloaded extends JavaPlugin {

    private static final int metricsId = 17668;

    private int uniqueId;
    private ItemMeta itemMeta;
    private VersionManager versionManager;
    private HookManager hookManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        // generate random id
        uniqueId = Utils.generateRandomId();

        // version manager
        this.versionManager = new VersionManager(getServer(), getLogger());

        // item meta
        this.itemMeta = new ItemMeta(this);

        // check version
        if (!versionManager.isServerSupport()) {
            setEnabled(false);
            return;
        }

        // hooks
        this.hookManager = new HookManager(getServer().getPluginManager());
        hookManager.registerHooks();

        // config
        this.configManager = new ConfigManager(this);
        configManager.loadAllConfig(getServer().getConsoleSender());

        // metrics
        new Metrics(this, metricsId);
    }




}
