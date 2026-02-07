package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded;

import io.avaje.inject.BeanScope;
import io.avaje.inject.Component;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.CommandTabCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.FJ2RCommandExecutor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.Configs;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.FJConfig;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.di.PluginFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc.FJVersion;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc.PluginMetrics;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

@Component.Import(value = PluginMetrics.class)
public class FJetpack extends JavaPlugin {

    private BeanScope beanScope;

    private Logger log;
    private FJConfig config;

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

        this.config = beanScope.get(FJConfig.class);
    }

    @Override
    public void onDisable() {
        if (this.beanScope != null) {
            this.beanScope.close();
        }
    }


    // NEED TO REWRITE
    /*@Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return CommandTabCompleter.onTab(sender, command, alias, args);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            return FJ2RCommandExecutor.onCommand(sender, command, label, args);
        } catch (Throwable e) {
            if (e instanceof NoPermissionLvlException) {
                Messages.sendMessage(sender, config.message().getNoPermission());
                return false;
            }
            if (e instanceof NumberFormatException) {
                Messages.sendMessage(sender, config.message().getInvalidNumber());
                return false;
            }
            log.error("Command error!", e);
        }
        return false;
    }*/

}
