package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded;

import io.avaje.inject.BeanScope;
import io.avaje.inject.Component;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.di.PluginFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.InfoLevelException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemDataProvider;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc.FJVersion;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc.PluginMetrics;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

@Component.Import(value = PluginMetrics.class)
public class FJetpack extends JavaPlugin {

    private BeanScope beanScope;

    private Logger log;
    private CommandTabCompleterPlugin commandTabCompleterPlugin;

    @Override
    public void onEnable() {
        this.beanScope = BeanScope.builder()
                .bean(PluginFactory.Initiator.class, new PluginFactory.Initiator(this, () -> this.beanScope))
                .build();

        // get logger
        this.log = beanScope.get(Logger.class);

        // check version
        val version = beanScope.get(FJVersion.class);

        log.info("&6Checking Server Version...");
        if (!version.isServerSupport()) {
            log.info("&cUnsupported Server Version {} - {}", getServer().getVersion(), getServer().getBukkitVersion());
            setEnabled(false);
            return;
        }
        log.info("&6Detected Server: &a{} v{}", getServer().getName(), version.getServerVersion());


        log.info("&6Checking Item Data Provider...");
        val itemDataProvider = beanScope.get(ItemDataProvider.class);
        if (!itemDataProvider.isSupported()) {
            log.info("&cItem Data Provider is not supported!. Please install &6NBTAPI &cplugin!");
            setEnabled(false);
            return;
        }
        log.info("&6Item Data Provider: &a{}", itemDataProvider.getClass().getSimpleName());

        this.commandTabCompleterPlugin = beanScope.get(CommandTabCompleterPlugin.class);
    }

    @Override
    public void onDisable() {
        if (this.beanScope != null) {
            this.beanScope.close();
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, @NonNull String[] args) {
        return this.commandTabCompleterPlugin.onTabComplete(sender, command, alias, args);
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        try {
            return this.commandTabCompleterPlugin.onCommand(sender, command, label, args);
        } catch (InfoLevelException e) {
            Messages.sendMessage(sender, e.getMessage());
            return false;
        }
    }

}
