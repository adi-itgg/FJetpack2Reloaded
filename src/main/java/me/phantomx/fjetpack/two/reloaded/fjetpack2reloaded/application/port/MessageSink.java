package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Output port for prefixed, color-translated messages.
 * <p>
 * Implemented by {@link Messages}. Infrastructure (e.g. the config loader) reports through this
 * port instead of depending on the concrete presentation class.
 */
@FunctionalInterface
public interface MessageSink {

    void send(@NotNull CommandSender target, @NotNull String message, Object @NotNull ... args);

}
