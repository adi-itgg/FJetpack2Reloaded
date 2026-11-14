package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application;

import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.IMessageException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Turns a throwable that escaped an event handler into feedback for whoever caused it.
 * <p>
 * Two of the plugin's own exceptions are a deliberate way to abort an event:
 * {@link NoPermissionLvlException} still needs a message, {@link IMessageException} was already
 * reported by whoever raised it. Anything else goes to the console, where the StackTrace is readable.
 * <p>
 * Only the two handlers that reported failures before still do; the rest stay silent on purpose,
 * which is what they did.
 */
@Singleton
public class ListenerFailureHandler {

    private final ConfigRepository configs;
    private final MessageSink messages;
    private final Log log;

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
            messages.send(target, configs.message().getNoPermission());
            return;
        }
        if (error instanceof IMessageException) return;
        messages.sendToConsole("ERROR " + eventName + ": %s", error);
    }

}
