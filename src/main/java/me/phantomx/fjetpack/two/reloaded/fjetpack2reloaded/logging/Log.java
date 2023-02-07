package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.logging;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Log {

    private static final boolean DEBUG = false;

    private final String tag;

    @Contract(pure = true)
    public <T> Log(@NotNull Class<T> clazz) {
        tag = clazz.getSimpleName();
    }

    public <T> Log(@NotNull String tag) {
        this.tag = tag;
    }

    public static void log(@Nullable Object message, Object... args) {
        if (!DEBUG) return;
        if (message instanceof String) {
            Messages.sendMessage((String) message, args);
            return;
        }
        val sb = new StringBuilder();
        sb.append(message);
        Messages.sendMessage(sb.toString(), args);
        sb.setLength(0);
    }

    public void debug(@Nullable Object message, Object... args) {
        Log.log(tag + " - " + message, args);
    }

}
