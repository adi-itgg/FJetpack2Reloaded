package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.executor.impl;

import io.avaje.inject.Component;
import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.executor.FJCommandExecutor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.FJConfig;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.JetpackItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Placeholder;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class GetOrGiveFuelFJCommandExecutor implements FJCommandExecutor {

    private final FJConfig config;
    private final Server server;
    private final JetpackItemFactory jetpackItemFactory;

    @Override
    public boolean isSupported(FJ2RCommand cmd) {
        return cmd == FJ2RCommand.GET_FUEL || cmd == FJ2RCommand.GIVE_FUEL;
    }

    @Override
    public void onCommand(CommandSender sender, FJ2RCommand cmd, String[] args) {
        if (args.length == 1) {
            Messages.sendMessage(sender, config.helpText().get(5));
            return;
        }

        var customFuelId = Arrays.stream(args).filter(arg -> config.customFuels().get(arg) != null)
                .findFirst().orElse(null);

        if (customFuelId == null) {
            Messages.sendMessage(sender, "&cInvalid custom fuel id!");
            return;
        }

        var targetPlayer = Arrays.stream(args).map(server::getPlayerExact)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> sender instanceof Player player ? player : null);

        if (targetPlayer == null) {
            Messages.sendMessage(sender, config.message().getCmdPlayerOnly());
            return;
        }

        var amount = Arrays.stream(args).filter(NumberUtils::isDigits)
                .map(NumberUtils::toInt)
                .findFirst()
                .orElse(0);

        var customFuelItem = jetpackItemFactory.createCustomFuelItem(customFuelId, amount);

        addItemToInventory(config, targetPlayer, customFuelItem);

        if (sender == targetPlayer) {
            var msg = config.message().getCmdFuelGiveSuccess()
                    .replace(Placeholder.CUSTOM_FUEL, customFuelId)
                    .replace(Placeholder.AMOUNT, String.valueOf(amount))
                    .replace(Placeholder.PLAYER, targetPlayer.getDisplayName())
                    .replace(Placeholder.SENDER, sender.getName());
            Messages.sendMessage(sender, msg);
            return;
        }

        // send message to sender
        var msg = config.message().getCmdFuelGiveSuccess()
                .replace(Placeholder.CUSTOM_FUEL, customFuelId)
                .replace(Placeholder.AMOUNT, String.valueOf(amount))
                .replace(Placeholder.PLAYER, targetPlayer.getDisplayName())
                .replace(Placeholder.SENDER, sender.getName());
        Messages.sendMessage(sender, msg);


        // send message to target player
        msg = config.message().getCmdFuelGiveReceived()
                .replace(Placeholder.CUSTOM_FUEL, customFuelId)
                .replace(Placeholder.AMOUNT, String.valueOf(amount))
                .replace(Placeholder.PLAYER, targetPlayer.getDisplayName())
                .replace(Placeholder.SENDER, sender.getName());
        Messages.sendMessage(targetPlayer, msg);
    }

}
