package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.JetpackGrant;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ProtectionGuard;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.CommandTabCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.HelpText;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.PlayerTarget;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.handler.CheckUpdateCommandHandler;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.handler.GetOrGiveCommandHandler;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.handler.GetOrGiveFuelCommandHandler;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.handler.HelpCommandHandler;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.handler.ReloadCommandHandler;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.handler.SetCommandHandler;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.handler.SetFuelCommandHandler;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.completer.GetOrGiveCommandCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.completer.GetOrGiveFuelCommandCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.completer.NoArgsCommandCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.completer.SetCommandCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.completer.SetFuelCommandCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommandExecutor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.PlayerRegistry;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener.RegisterEvent;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook.SuperiorSkyblockEvent;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.item.ItemTagCodec;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.logging.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util.UpdateChecker;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util.Version;
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
        val helpText = new HelpText(this);
        val targets = new PlayerTarget(messages, configs);
        this.commandExecutor = new FJ2RCommandExecutor(messages, List.of(
                new ReloadCommandHandler(configs),
                new CheckUpdateCommandHandler(updateChecker),
                new SetCommandHandler(messages, configs, items, targets, helpText),
                new SetFuelCommandHandler(messages, configs, items, targets, helpText),
                new GetOrGiveCommandHandler(messages, configs, grant, targets, helpText),
                new GetOrGiveFuelCommandHandler(messages, configs, grant, targets, helpText)
        ), new HelpCommandHandler(messages, helpText));
        this.tabCompleter = new CommandTabCompleter(messages, List.of(
                new NoArgsCommandCompleter(FJ2RCommand.HELP, FJ2RCommand.RELOAD, FJ2RCommand.CHECK_UPDATE),
                new SetCommandCompleter(configs),
                new SetFuelCommandCompleter(),
                new GetOrGiveCommandCompleter(configs),
                new GetOrGiveFuelCommandCompleter(configs)
        ));

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
