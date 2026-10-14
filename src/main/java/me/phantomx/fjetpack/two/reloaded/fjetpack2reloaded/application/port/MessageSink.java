package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Output port for prefixed, color-translated messages.
 * <p>
 * Implemented by {@link Messages}. Infrastructure (e.g. the config loader) reports through this
 * port instead of depending on the concrete presentation class.
 */
public interface MessageSink {

    /**
     * @param message null or empty when the key is not configured in message.yml. Nothing is sent
     *                in that case rather than an exception: a missing line must not break an
     *                operation, and a silent one is easier to notice than a stack trace per tick.
     */
    void send(@NotNull CommandSender target, @Nullable String message, Object... args);

    /**
     * The configured message prefix, pushed by the config repository the moment message.yml is
     * loaded. Pushing it keeps the port free of a back-reference to the repository, so the two
     * beans do not form a cycle.
     */
    void usePrefix(@NotNull String prefix);

    /** Console output, for infrastructure warnings and startup lines. */
    default void sendToConsole(@Nullable String message, Object... args) {
        send(Bukkit.getConsoleSender(), message, args);
    }

}
