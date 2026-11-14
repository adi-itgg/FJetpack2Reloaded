package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.handler;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Permissions;
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

import java.util.Arrays;
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
    private final Log log;

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
        this.log = new Log(getClass(), messages);
    }

    @Override
    public @NotNull Set<FJ2RCommand> commands() {
        return Set.of(FJ2RCommand.SET_FUEL);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        val perm = FJ2RCommand.SET_FUEL.literal();
        log.debug("setfuel enter: sender=%s op=%s args=%s serverVersion=%d api=%s permission=%s allowed=%s",
                sender.getName(), sender.isOp(), Arrays.toString(args), version.getServerVersion(),
                version.getNmsApiVersion(), Permissions.PERMISSION_PREFIX + perm, Permissions.hasPermission(sender, perm));
        requirePermission(sender, FJ2RCommand.SET_FUEL);
        log.debug("setfuel: permission ok");

        // if args is empty should send how to use this cmd
        if (args.length == 1) {
            log.debug("setfuel: no amount argument, showing usage");
            messages.sendMessage(sender, helpText.line(8));
            return;
        }

        val player = targets.asPlayer(sender);
        if (player == null) {
            log.debug("setfuel: sender=%s is not a player", sender.getName());
            return;
        }

        val item = items.itemInHand(player);
        if (item == null) {
            log.debug("setfuel: player=%s has no usable item in hand", player.getName());
            messages.sendMessage(configs.message().getNoItemInMainHand());
            return;
        }

        val fuelAmount = NumberUtils.toLong(args[1], 0);
        log.debug("setfuel: player=%s item=%s amountArg=%s parsedFuel=%d",
                player.getName(), Log.item(item), args[1], fuelAmount);

        val jetpackId = items.jetpackIdOf(item);
        if (jetpackId.isEmpty()) {
            log.debug("setfuel: player=%s item=%s has no jetpack id tag", player.getName(), Log.item(item));
            messages.sendMessage(sender, configs.message().getNotJetpackItem());
            return;
        }

        log.debug("setfuel: applying player=%s jetpackId=%s fuel=%d", player.getName(), jetpackId, fuelAmount);
        items.setItemAsJetpack(sender, item, jetpackId, fuelAmount)
                .onSuccess(result -> {
                    log.debug("setfuel: success player=%s jetpackId=%s fuel=%d result=%s",
                            player.getName(), jetpackId, fuelAmount, Log.item(result));
                    if (version.getServerVersion() > 11) {
                        log.debug("setfuel: writing result with setItemInMainHand (serverVersion=%d)", version.getServerVersion());
                        player.getInventory().setItemInMainHand(result);
                    } else {
                        log.debug("setfuel: writing result with setItemInHand (serverVersion=%d)", version.getServerVersion());
                        player.setItemInHand(result);
                    }
                    messages.sendMessage(sender, configs.message().getCmdFuelSet()
                            .replace(Placeholder.AMOUNT, String.valueOf(fuelAmount))
                    );
                })
                .onFailure(error -> log.debug("setfuel FAILED: sender=%s player=%s jetpackId=%s fuel=%d: %s",
                        sender.getName(), player.getName(), jetpackId, fuelAmount, error))
                .throwIfError();
    }

}
