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
public class GetOrGiveFJCommandExecutor implements FJCommandExecutor {

    private final FJConfig config;
    private final Server server;
    private final JetpackItemFactory jetpackItemFactory;

    @Override
    public boolean isSupported(FJ2RCommand cmd) {
        return cmd == FJ2RCommand.GET || cmd == FJ2RCommand.GIVE;
    }

    @Override
    public void onCommand(CommandSender sender, FJ2RCommand cmd, String[] args) {
        if (args.length == 1) {
            Messages.sendMessage(sender, config.helpText().get(4));
            return;
        }

        var jetpackId = Arrays.stream(args)
                .filter(arg -> config.jetpacks().get(arg) != null)
                .findFirst()
                .orElse(null);

        if (jetpackId == null) {
            Messages.sendMessage(sender, "&cUnknown jetpack id");
            return;
        }

        var targetPlayer = Arrays.stream(args)
                .map(server::getPlayerExact)
                .filter(Objects::nonNull)
                .findFirst().orElseGet(() -> {
                    if (sender instanceof Player player) {
                        return player;
                    }
                    return null;
                });

        if (targetPlayer == null) {
            Messages.sendMessage(config.message().getCmdPlayerOnly());
            return;
        }

        long fuelAmount = Arrays.stream(args)
                .filter(NumberUtils::isDigits)
                .map(NumberUtils::toLong)
                .findFirst().orElse(0L);

        if (fuelAmount < 0) {
            Messages.sendMessage(sender, "&cFuel amount must be a positive integer");
            return;
        }

        var jetpackItem = jetpackItemFactory.createJetpackItem(sender, jetpackId, fuelAmount);
        addItemToInventory(config, targetPlayer, jetpackItem);

        if (sender == targetPlayer) {
            Messages.sendMessage(sender, config.message().getCmdGetSelf());
            return;
        }

        // notify sender
        var msg = config.message().getCmdGiveSuccess()
                .replace(Placeholder.JETPACK, jetpackId)
                .replace(Placeholder.FUEL_VALUE, String.valueOf(fuelAmount))
                .replace(Placeholder.PLAYER, targetPlayer.getDisplayName());
        Messages.sendMessage(sender, msg);


        // notify target player
        msg = config.message().getCmdGiveReceived()
                .replace(Placeholder.JETPACK, jetpackId)
                .replace(Placeholder.FUEL_VALUE, String.valueOf(fuelAmount))
                .replace(Placeholder.PLAYER, targetPlayer.getDisplayName())
                .replace(Placeholder.SENDER, sender.getName());
        Messages.sendMessage(targetPlayer, msg);
    }

}
