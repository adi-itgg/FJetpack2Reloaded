package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.completer;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Tab completion rules of one subcommand (or of a family of subcommands that share the same rules).
 */
public interface CommandCompleter extends SubCommand {

    /** Suggestions for the arguments after the subcommand. Never null. */
    @NotNull List<String> complete(@NotNull CommandSender sender, @NotNull String[] args);

}
