package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application;

import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.IMessageException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Turns a throwable that escaped an event handler into feedback for whoever caused it.
 * <p>
 * Two of the plugin's own exceptions are a deliberate way to abort an event:
 * {@link NoPermissionLvlException} still needs a message, {@link IMessageException} was already
 * reported by whoever raised it. Anything else goes to the console, where the StackTrace is readable.
 * <p>
 * A listener routes its whole handler here, so an unexpected throwable from the same class is
 * reported too instead of disappearing.
 */
@Singleton
public class ListenerFailureHandler {

    /** A denial is a normal outcome: one console line per player per window keeps it from flooding. */
    private static final long DENIAL_LOG_INTERVAL_MS = 5_000L;

    private final ConfigRepository configs;
    private final MessageSink messages;
    private final Log log;
    private final Map<UUID, Long> lastDenial = new ConcurrentHashMap<>();

    public ListenerFailureHandler(@NotNull ConfigRepository configs, @NotNull MessageSink messages) {
        this.configs = configs;
        this.messages = messages;
        this.log = new Log(getClass(), messages);
    }

    /**
     * @param error     what escaped the handler
     * @param target    who hears about it
     * @param eventName the Bukkit event name, used to label the console line
     */
    public void handle(@NotNull Throwable error, @NotNull CommandSender target, @NotNull String eventName) {
        log.warn("event failure: event=%s target=%s error=%s: %s", eventName, target.getName(),
                error.getClass().getName(), error);
        if (error instanceof NoPermissionLvlException) {
            messages.send(target, configs.message().getNoPermission() + " &7(" + error.getMessage() + ")");
            logDenial(target, error);
            return;
        }
        if (error instanceof IMessageException) return;
        messages.sendToConsole("ERROR " + eventName + ": %s", error);
    }

    /**
     * The reason has to survive a build that is not in debug mode - that is where an operator looks
     * when a player reports "it will not turn on" - but sneak and inventory clicks are spammable, so
     * the line is throttled per player instead of printed once per attempt.
     */
    private void logDenial(@NotNull CommandSender target, @NotNull Throwable error) {
        long now = System.currentTimeMillis();
        if (target instanceof Player player) {
            Long last = lastDenial.put(player.getUniqueId(), now);
            if (last != null && now - last < DENIAL_LOG_INTERVAL_MS) return;
        }
        messages.sendToConsole("&e%s was denied: %s", target.getName(), error.getMessage());
    }

}
