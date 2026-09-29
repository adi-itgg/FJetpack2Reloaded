package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.handler;

import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.HelpText;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.message.Messages;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/** {@code /fj2r help} - also used for an unknown subcommand. */
@Singleton
public class HelpCommandHandler implements CommandHandler {

    private final Messages messages;
    private final HelpText helpText;

    public HelpCommandHandler(@NotNull Messages messages, @NotNull HelpText helpText) {
        this.messages = messages;
        this.helpText = helpText;
    }

    @Override
    public @NotNull Set<FJ2RCommand> commands() {
        return Set.of(FJ2RCommand.HELP);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        requirePermission(sender, FJ2RCommand.HELP);
        messages.sendMessage(false, sender, helpText.getText());
    }

}
