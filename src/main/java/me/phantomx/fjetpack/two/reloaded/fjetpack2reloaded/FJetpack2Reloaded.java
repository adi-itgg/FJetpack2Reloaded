package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.JetpackGrant;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ProtectionGuard;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.CommandTabCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.FJ2RCommandExecutor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.PlayerRegistry;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.event.RegisterEvent;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.hook.event.SuperiorSkyblockEvent;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemTagCodec;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.logging.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.UpdateChecker;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Version;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Composition root: builds the object graph once and hands it to the Bukkit entry points.
 * No static state is used to reach collaborators.
 */
public class FJetpack2Reloaded extends JavaPlugin {

    private static final int METRICS_ID = 17668;

    private Messages messages;
    private YamlConfigRepository configs;
    private ItemTagCodec itemTags;
    private ItemFactory items;
    private PlayerRegistry players;
    private UpdateChecker updateChecker;
    private FJ2RCommandExecutor commandExecutor;
    private CommandTabCompleter tabCompleter;
    private Log log;

    @Override
    public void onEnable() {
        this.messages = new Messages(() -> configs == null ? null : configs.message());
        this.configs = new YamlConfigRepository(this, messages);
        this.log = new Log(getClass(), messages);
        this.itemTags = new ItemTagCodec(this);
        this.items = new ItemFactory(configs, itemTags, messages);
        this.players = new PlayerRegistry(this, configs, messages, itemTags,
                new ProtectionGuard(configs, messages));
        this.updateChecker = new UpdateChecker(this, messages);
        val grant = new JetpackGrant(configs, messages, items);
        this.commandExecutor = new FJ2RCommandExecutor(this, configs, messages, items, grant, updateChecker);
        this.tabCompleter = new CommandTabCompleter(configs, messages);

        val serverSupported = Version.isServerSupport(messages, itemTags);
        log.log("is server support: %b", serverSupported);
        if (!serverSupported) {
            setEnabled(false);
            return;
        }

        getServer().getPluginManager().registerEvents(new SuperiorSkyblockEvent(this, configs, messages, players), this);
        getServer().getPluginManager().registerEvents(
                new RegisterEvent(players, configs, items, itemTags, messages, updateChecker), this);

        configs.reload();

        new Metrics(this, METRICS_ID);
    }

    @Override
    public void onDisable() {
        players.clear();
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
