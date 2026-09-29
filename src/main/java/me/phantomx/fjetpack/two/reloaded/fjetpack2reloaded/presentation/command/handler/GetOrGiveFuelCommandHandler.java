package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.handler;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.JetpackGrant;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.HelpText;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.PlayerTarget;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.message.Messages;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

/** {@code /fj2r getfuel|givefuel [player] [customFuel] [amount]} */
@Singleton
public class GetOrGiveFuelCommandHandler implements CommandHandler {

    private final Messages messages;
    private final YamlConfigRepository configs;
    private final JetpackGrant grant;
    private final PlayerTarget targets;
    private final HelpText helpText;

    public GetOrGiveFuelCommandHandler(@NotNull Messages messages,
                                       @NotNull YamlConfigRepository configs,
                                       @NotNull JetpackGrant grant,
                                       @NotNull PlayerTarget targets,
                                       @NotNull HelpText helpText) {
        this.messages = messages;
        this.configs = configs;
        this.grant = grant;
        this.targets = targets;
        this.helpText = helpText;
    }

    @Override
    public @NotNull Set<FJ2RCommand> commands() {
        return Set.of(FJ2RCommand.GET_FUEL, FJ2RCommand.GIVE_FUEL);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        requireAnyPermission(sender, FJ2RCommand.GET_FUEL, FJ2RCommand.GIVE_FUEL);

        if (args.length == 1) {
            messages.sendMessage(sender, helpText.line(6));
            return;
        }

        val customFuelId = Arrays.stream(args)
                .filter(arg -> configs.customFuels().get(arg) != null)
                .findFirst()
                .orElse(null);

        if (customFuelId == null) {
            messages.sendMessage(sender, "&cInvalid custom fuel id!");
            return;
        }

        val targetPlayer = targets.of(sender, args);
        if (targetPlayer == null) return;

        val amount = Arrays.stream(args)
                .filter(NumberUtils::isDigits)
                .map(NumberUtils::toInt)
                .findFirst().orElse(1);

        grant.giveCustomFuel(sender, targetPlayer, customFuelId, amount);
    }

}
