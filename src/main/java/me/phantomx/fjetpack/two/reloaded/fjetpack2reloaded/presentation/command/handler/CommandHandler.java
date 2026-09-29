package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.handler;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util.Permissions;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Behaviour of one subcommand (or of a family of subcommands that share the same behaviour).
 * <p>
 * Handlers own the argument rules; they never deal with events or the Bukkit command plumbing.
 */
public interface CommandHandler extends SubCommand {

    void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args);

    /** Shared permission gate: the literal of the subcommand is the permission node. */
    default void requirePermission(@NotNull CommandSender sender, @NotNull FJ2RCommand command) {
        if (!Permissions.hasPermission(sender, command.literal()))
            NoPermissionLvlException.send();
    }

    /** Shared gate for a family of subcommands that share one behaviour: any of them is enough. */
    default void requireAnyPermission(@NotNull CommandSender sender, FJ2RCommand @NotNull ... commands) {
        for (FJ2RCommand command : commands)
            if (Permissions.hasPermission(sender, command.literal())) return;
        NoPermissionLvlException.send();
    }

}
