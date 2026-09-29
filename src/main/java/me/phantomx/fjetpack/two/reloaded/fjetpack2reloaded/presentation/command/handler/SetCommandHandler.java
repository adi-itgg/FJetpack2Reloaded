package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.handler;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.Placeholder;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util.Version;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.HelpText;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.PlayerTarget;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.message.Messages;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/** {@code /fj2r set <jetpack> <fuel>} - turn the item in hand into a jetpack. */
@Singleton
public class SetCommandHandler implements CommandHandler {

    private final Messages messages;
    private final YamlConfigRepository configs;
    private final ItemFactory items;
    private final PlayerTarget targets;
    private final HelpText helpText;

    public SetCommandHandler(@NotNull Messages messages,
                             @NotNull YamlConfigRepository configs,
                             @NotNull ItemFactory items,
                             @NotNull PlayerTarget targets,
                             @NotNull HelpText helpText) {
        this.messages = messages;
        this.configs = configs;
        this.items = items;
        this.targets = targets;
        this.helpText = helpText;
    }

    @Override
    public @NotNull Set<FJ2RCommand> commands() {
        return Set.of(FJ2RCommand.SET);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        requirePermission(sender, FJ2RCommand.SET);

        // if args is empty should send how to use this cmd
        if (args.length == 1) {
            messages.sendMessage(sender, helpText.line(7));
            return;
        }

        val player = targets.asPlayer(sender);
        if (player == null) return;

        val item = items.itemInHand(player);
        if (item == null) {
            messages.sendMessage(configs.message().getNoItemInMainHand());
            return;
        }

        items.setItemAsJetpack(sender, item, args[1], args.length == 3 ? Long.parseLong(args[2]) : 0)
                .onSuccess(result -> {
                    if (Version.getServerVersion() > 11)
                        player.getInventory().setItemInMainHand(result);
                    else
                        player.setItemInHand(result);
                    messages.sendMessage(sender, configs.message().getCmdSet()
                            .replace(Placeholder.JETPACK, args[1])
                            .replace(Placeholder.FUEL_VALUE, String.valueOf(args.length == 3 ? args[2] : 0))
                    );
                }).throwIfError();
    }

}
