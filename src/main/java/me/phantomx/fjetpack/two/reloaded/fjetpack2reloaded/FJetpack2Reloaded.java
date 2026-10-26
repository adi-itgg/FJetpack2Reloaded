package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded;

import io.avaje.inject.BeanScope;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.di.PluginFactory;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.PlayerRegistry;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ServerSupport;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook.SuperiorSkyblockEvent;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.CommandTabCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommandExecutor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener.RegisterEvent;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.message.Messages;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Bukkit entry point. The object graph is built by Avaje Inject - this class only hands over the
 * plugin instance (which Bukkit creates) and pulls the few beans the Bukkit callbacks need.
 */
public class FJetpack2Reloaded extends JavaPlugin {

    private static final int METRICS_ID = 17668;

    private BeanScope beanScope;
    private Messages messages;
    private YamlConfigRepository configs;
    private PlayerRegistry players;
    private FJ2RCommandExecutor commandExecutor;
    private CommandTabCompleter tabCompleter;

    @Override
    public void onEnable() {
        this.beanScope = BeanScope.builder()
                .bean(PluginFactory.Initiator.class, new PluginFactory.Initiator(this))
                .build();

        this.messages = beanScope.get(Messages.class);
        this.configs = beanScope.get(YamlConfigRepository.class);
        this.players = beanScope.get(PlayerRegistry.class);
        this.commandExecutor = beanScope.get(FJ2RCommandExecutor.class);
        this.tabCompleter = beanScope.get(CommandTabCompleter.class);

        val log = new Log(getClass(), messages);
        val serverSupported = beanScope.get(ServerSupport.class).check();
        log.log("is server support: %b", serverSupported);
        if (!serverSupported) {
            closeScope();
            setEnabled(false);
            return;
        }

        val superiorSkyblock = beanScope.get(SuperiorSkyblockEvent.class);
        superiorSkyblock.hook();
        getServer().getPluginManager().registerEvents(superiorSkyblock, this);
        getServer().getPluginManager().registerEvents(beanScope.get(RegisterEvent.class), this);

        reloadAndReport(log);

        new Metrics(this, METRICS_ID);
    }

    @Override
    public void onDisable() {
        players.clear();
        closeScope();
    }

    /**
     * Loads the files and says out loud when the result cannot be used.
     * <p>
     * A broken file keeps whatever was already loaded, so a reload never breaks a running server. At
     * startup there is no "already loaded" to fall back on: every jetpack comes from jetpack.yml, so
     * a file that does not map leaves the plugin running with nothing to fly with.
     * <p>
     * It stays enabled on purpose. Disabling would also disable {@code /fj2r reload}, which is the
     * one command that fixes the situation - so the failure is reported, and the server keeps a way
     * out of it.
     */
    private void reloadAndReport(@NotNull Log log) {
        configs.reload();

        val failed = configs.failedFiles();
        if (failed.isEmpty() && !configs.jetpacks().isEmpty()) return;

        log.log("config problems: failed=%s, jetpacks=%d", failed, configs.jetpacks().size());
        messages.send(Bukkit.getConsoleSender(),
                "&cNo jetpack could be loaded%s. Check the files in &6%s&c, then run &6/fj2r reload&c.",
                failed.isEmpty() ? "" : " (could not read " + failed + ")",
                getDataFolder()
        );
    }

    /** Closing twice is never needed: the scope is dropped once it has been closed. */
    private void closeScope() {
        if (beanScope == null) return;
        beanScope.close();
        beanScope = null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Catcher.create(() -> commandExecutor.onCommand(sender, command, label, args))
                .onFailure(error -> {
                    if (error instanceof NoPermissionLvlException) {
                        messages.sendMessage(sender, configs.message().getNoPermission());
                        return;
                    }
                    if (error instanceof NumberFormatException) {
                        messages.sendMessage(sender, configs.message().getInvalidNumber());
                        return;
                    }
                    messages.sendMessage("Command error!: %s", error);
                })
                .getOrDefault(false);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return tabCompleter.onTab(sender, command, alias, args);
    }

}
