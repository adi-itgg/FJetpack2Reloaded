package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.completer;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * Tab completion rules of one subcommand (or of a family of subcommands that share the same rules).
 */
public interface CommandCompleter {

    /** Subcommands this completer answers for. */
    @NotNull Set<FJ2RCommand> commands();

    /** Suggestions for the arguments after the subcommand. Never null. */
    @NotNull List<String> complete(@NotNull CommandSender sender, @NotNull String @NotNull [] args);

    default boolean supports(@NotNull String command) {
        for (FJ2RCommand candidate : commands())
            if (candidate.literal().equalsIgnoreCase(command)) return true;
        return false;
    }

}
