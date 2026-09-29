package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.handler;

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

/** {@code /fj2r get|give [player] [jetpack] [fuel]} - arguments may come in any order. */
public class GetOrGiveCommandHandler implements CommandHandler {

    private final Messages messages;
    private final YamlConfigRepository configs;
    private final JetpackGrant grant;
    private final PlayerTarget targets;
    private final HelpText helpText;

    public GetOrGiveCommandHandler(@NotNull Messages messages,
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
        return Set.of(FJ2RCommand.GET, FJ2RCommand.GIVE);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        requireAnyPermission(sender, FJ2RCommand.GET, FJ2RCommand.GIVE);

        if (args.length == 1) {
            messages.sendMessage(sender, helpText.line(5));
            return;
        }

        val jetpackId = Arrays.stream(args)
                .filter(arg -> configs.jetpacks().get(arg) != null)
                .findFirst()
                .orElse(null);

        if (jetpackId == null) {
            messages.sendMessage(sender, "&cJetpack didn't exist");
            return;
        }

        val targetPlayer = targets.of(sender, args);
        if (targetPlayer == null) return;

        val fuelAmount = Arrays.stream(args)
                .filter(NumberUtils::isDigits)
                .map(NumberUtils::toInt)
                .findFirst().orElse(0);

        grant.giveJetpack(sender, targetPlayer, jetpackId, fuelAmount);
    }

}
