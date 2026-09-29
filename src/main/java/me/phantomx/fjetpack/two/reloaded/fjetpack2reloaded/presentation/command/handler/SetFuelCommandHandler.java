package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.handler;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.Placeholder;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ServerVersion;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.HelpText;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.PlayerTarget;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.message.Messages;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/** {@code /fj2r setfuel <amount>} - set the fuel of the jetpack in hand. */
@Singleton
public class SetFuelCommandHandler implements CommandHandler {

    private final Messages messages;
    private final YamlConfigRepository configs;
    private final ItemFactory items;
    private final PlayerTarget targets;
    private final HelpText helpText;
    private final ServerVersion version;

    public SetFuelCommandHandler(@NotNull Messages messages,
                                 @NotNull YamlConfigRepository configs,
                                 @NotNull ItemFactory items,
                                 @NotNull PlayerTarget targets,
                                 @NotNull HelpText helpText,
                             @NotNull ServerVersion version) {
        this.messages = messages;
        this.configs = configs;
        this.items = items;
        this.targets = targets;
        this.helpText = helpText;
        this.version = version;
    }

    @Override
    public @NotNull Set<FJ2RCommand> commands() {
        return Set.of(FJ2RCommand.SET_FUEL);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        requirePermission(sender, FJ2RCommand.SET_FUEL);

        // if args is empty should send how to use this cmd
        if (args.length == 1) {
            messages.sendMessage(sender, helpText.line(8));
            return;
        }

        val player = targets.asPlayer(sender);
        if (player == null) return;

        val item = items.itemInHand(player);
        if (item == null) {
            messages.sendMessage(configs.message().getNoItemInMainHand());
            return;
        }

        val fuelAmount = NumberUtils.toLong(args[1], 0);

        val jetpackId = items.jetpackIdOf(item);
        if (jetpackId.isEmpty()) {
            messages.sendMessage(sender, configs.message().getNotJetpackItem());
            return;
        }

        items.setItemAsJetpack(sender, item, jetpackId, fuelAmount)
                .onSuccess(result -> {
                    if (version.getServerVersion() > 11)
                        player.getInventory().setItemInMainHand(result);
                    else
                        player.setItemInHand(result);
                    messages.sendMessage(sender, configs.message().getCmdFuelSet()
                            .replace(Placeholder.AMOUNT, String.valueOf(fuelAmount))
                    );
                }).throwIfError();
    }

}
