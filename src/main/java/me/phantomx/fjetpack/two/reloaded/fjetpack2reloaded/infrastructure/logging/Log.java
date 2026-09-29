package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.logging;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Log {

    private static final boolean DEBUG = Bukkit.getMotd().contains("[DEV-FJ2R-DEBUG]");

    private final String tag;
    private final MessageSink messages;

    public Log(@NotNull Class<?> clazz, @NotNull MessageSink messages) {
        this(clazz.getSimpleName(), messages);
    }

    public Log(@NotNull String tag, @NotNull MessageSink messages) {
        this.tag = tag;
        this.messages = messages;
    }

    public void log(@Nullable Object message, Object... args) {
        if (!DEBUG) return;
        if (message instanceof String text) {
            messages.sendToConsole(text, args);
            return;
        }
        val sb = new StringBuilder();
        sb.append(message);
        messages.sendToConsole(sb.toString(), args);
        sb.setLength(0);
    }

    public void debug(@Nullable Object message, Object... args) {
        log(tag + " - " + message, args);
    }

}
