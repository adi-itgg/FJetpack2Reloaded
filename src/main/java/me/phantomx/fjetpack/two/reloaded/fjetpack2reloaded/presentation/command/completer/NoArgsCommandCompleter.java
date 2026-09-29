package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.completer;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/** Subcommands without further arguments. */
public class NoArgsCommandCompleter implements CommandCompleter {

    private final Set<FJ2RCommand> commands;

    public NoArgsCommandCompleter(FJ2RCommand @NotNull ... commands) {
        this.commands = Set.of(commands);
    }

    @Override
    public @NotNull Set<FJ2RCommand> commands() {
        return commands;
    }

    @Override
    public @NotNull List<String> complete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return List.of();
    }

}
