package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.logging.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.handler.CommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Bukkit command entry point: finds the {@link CommandHandler} of the typed subcommand and lets it
 * run. Anything unknown - including {@code help} - falls back to the help handler.
 */
public class FJ2RCommandExecutor {

    private final Log log;
    private final List<CommandHandler> handlers;
    private final CommandHandler helpFallback;

    public FJ2RCommandExecutor(@NotNull MessageSink messages,
                               @NotNull List<CommandHandler> handlers,
                               @NotNull CommandHandler helpFallback) {
        this.log = new Log(getClass(), messages);
        this.handlers = handlers;
        this.helpFallback = helpFallback;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        log.debug("onCommand: Sender=%s, Command=%s, label=%s, args=%s", sender.getName(), command.getName(), label, args);

        if (args.length > 0)
            for (CommandHandler handler : handlers)
                if (handler.supports(args[0])) {
                    handler.execute(sender, args);
                    return true;
                }

        // "help" or an unknown subcommand: both show the help text
        helpFallback.execute(sender, args);
        return true;
    }

}
