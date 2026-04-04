package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded;

import io.avaje.inject.BeanScope;
import io.avaje.inject.Component;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.completer.FJCommandTabCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.executor.FJCommandExecutor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.FJConfig;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.di.PluginFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.AccessDeniedLevelException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.InfoLevelException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemDataProvider;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc.FJVersion;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc.PluginMetrics;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.bukkit.util.StringUtil.copyPartialMatches;

@Component.Import(value = PluginMetrics.class)
public class FJetpack extends JavaPlugin {

    private BeanScope beanScope;

    private Logger log;
    private FJCommandTabCompleter completer;
    private FJCommandExecutor executor;
    private FJConfig config;

    @Override
    public void onEnable() {
        this.beanScope = BeanScope.builder()
                .bean(PluginFactory.Initiator.class, new PluginFactory.Initiator(this, () -> this.beanScope))
                .build();

        // get logger
        this.log = beanScope.get(Logger.class);

        // check version
        var version = beanScope.get(FJVersion.class);

        log.info("&6Checking Server Version...");
        if (!version.isServerSupport()) {
            log.info("&cUnsupported Server Version {} - {}", getServer().getVersion(), getServer().getBukkitVersion());
            setEnabled(false);
            return;
        }
        log.info("&6Detected Server: &a{} v{}", getServer().getName(), version.getServerVersion());


        log.info("&6Checking Item Data Provider...");
        var itemDataProvider = beanScope.get(ItemDataProvider.class);
        if (!itemDataProvider.isSupported()) {
            log.info("&cItem Data Provider is not supported!. Please install &6NBTAPI &cplugin!");
            setEnabled(false);
            return;
        }
        log.info("&6Item Data Provider: &a{}", itemDataProvider.getClass().getSimpleName());

        this.completer = beanScope.get(FJCommandTabCompleter.class);
        this.executor = beanScope.get(FJCommandExecutor.class);
        this.config = beanScope.get(FJConfig.class);

        log.info("&6Plugin Enabled!");
    }

    @Override
    public void onDisable() {
        log.info("&6Disabling plugin!");
        if (this.beanScope != null) {
            this.beanScope.close();
        }
        log.info("&6Plugin Disabled!");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, @NonNull String[] args) {
        if (args.length == 1) {
            return copyPartialMatches(args[0], FJ2RCommand.COMMANDS.stream().filter(cmd -> Permissions.hasPermission(sender, cmd)).toList(), new ArrayList<>());
        }

        var cmd = FJ2RCommand.parse(args[0]);

        // unknown command
        if (cmd == null || !Permissions.hasPermission(sender, cmd)) {
            return Collections.emptyList();
        }

        return completer.onTabComplete(sender, cmd, args);
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        try {
            if (args.length == 0) {
                return true;
            }

            val cmd = FJ2RCommand.parse(args[0]);

            // unknown command
            if (cmd == null) {
                Messages.sendMessage(false, sender, String.join("", config.helpText()));
                return true;
            }

            if (!Permissions.hasPermission(sender, cmd)) {
                throw new AccessDeniedLevelException(cmd.cmd());
            }

            executor.onCommand(sender, cmd, args);
            return true;
        } catch (InfoLevelException e) {
            Messages.sendMessage(sender, e.getMessage());
            return false;
        } catch (AccessDeniedLevelException e) {
            Messages.sendMessage(sender, config.message().getNoPermission());
            return false;
        }
    }

}
