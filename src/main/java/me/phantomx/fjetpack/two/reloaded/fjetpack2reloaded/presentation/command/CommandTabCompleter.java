package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command;

import jakarta.inject.Singleton;
import lombok.Getter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Permissions;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.completer.CommandCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.completer.Completions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Bukkit tab completion entry point: filters the subcommands the sender may use, then hands the
 * arguments to the {@link CommandCompleter} registered for that subcommand.
 */
@Singleton
public class CommandTabCompleter {

    @Getter(lazy = true)
    private static final List<String> commands = Arrays.stream(FJ2RCommand.values()).map(FJ2RCommand::literal).toList();

    private final Log log;
    private final List<CommandCompleter> completers;

    public CommandTabCompleter(@NotNull MessageSink messages, @NotNull List<CommandCompleter> completers) {
        this.log = new Log(getClass(), messages);
        this.completers = completers;
    }

    public @NotNull List<String> onTab(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        log.log("onTab - sender=%s, command=%s, alias=%s, args=%s, argsLength=%s", sender.getName(), command.getName(), alias, args, args.length);
        if (args.length == 1)
            return Completions.partial(args[0],
                    getCommands()
                            .stream()
                            .filter(cmd -> Permissions.hasPermission(sender, cmd))
                            .toList()
            );

        if (!Permissions.hasPermission(sender, args[0]))
            return Collections.emptyList();

        for (CommandCompleter completer : completers)
            if (completer.supports(args[0]))
                return completer.complete(sender, args);

        return Collections.emptyList();
    }

}
