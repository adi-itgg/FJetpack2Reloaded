package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Output port for prefixed, color-translated messages.
 * <p>
 * Implemented by {@link Messages}. Infrastructure (e.g. the config loader) reports through this
 * port instead of depending on the concrete presentation class.
 */
public interface MessageSink {

    void send(@NotNull CommandSender target, @NotNull String message, Object... args);

    /**
     * The configured message prefix, pushed by the config repository the moment message.yml is
     * loaded. Pushing it keeps the port free of a back-reference to the repository, so the two
     * beans do not form a cycle.
     */
    void usePrefix(@NotNull String prefix);

    /** Console output, for infrastructure warnings and startup lines. */
    default void sendToConsole(@NotNull String message, Object... args) {
        send(Bukkit.getConsoleSender(), message, args);
    }

}
