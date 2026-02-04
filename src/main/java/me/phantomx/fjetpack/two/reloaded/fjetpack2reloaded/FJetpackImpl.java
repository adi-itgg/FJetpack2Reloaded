package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded;

import io.avaje.inject.BeanScope;
import io.avaje.inject.Component;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.di.PluginFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc.FJVersion;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc.PluginMetrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

@Component.Import(value = PluginMetrics.class)
public class FJetpackImpl extends JavaPlugin implements FJetpack {

    private BeanScope beanScope;


    @Override
    public void onEnable() {
        this.beanScope = BeanScope.builder()
                .bean(PluginFactory.Provider.class, new PluginFactory.Provider(this))
                .build();

        // get logger
        val log = beanScope.get(Logger.class);

        // check version
        val version = beanScope.get(FJVersion.class);

        log.info("&6Checking Server Version...");
        if (!version.isServerSupport()) {
            log.info("&cUnsupported Server Version {} - {}", getServer().getVersion(), getServer().getBukkitVersion());
            setEnabled(false);
            return;
        }
        log.info("&6Detected Server: &a{} v{}", getServer().getName(), version.getServerVersion());
    }

    @Override
    public void onDisable() {
        if (this.beanScope != null) {
            this.beanScope.close();
        }
    }

}
