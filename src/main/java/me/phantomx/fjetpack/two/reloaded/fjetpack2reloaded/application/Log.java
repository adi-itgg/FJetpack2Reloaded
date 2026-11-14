package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Tagged output for one class. The debug flag is read once per instance, on construction: a static
 * initializer that talks to the server would run at an arbitrary time, and a static cache would be
 * invisible to a test. The check also survives running without a server (unit tests), where debug
 * logging is simply off.
 */
public class Log {

    private final String tag;
    private final MessageSink messages;
    private final boolean debug;

    public Log(@NotNull Class<?> clazz, @NotNull MessageSink messages) {
        this(clazz.getSimpleName(), messages);
    }

    public Log(@NotNull String tag, @NotNull MessageSink messages) {
        this.tag = tag;
        this.messages = messages;
        val server = Bukkit.getServer();
        this.debug = server != null && server.getMotd().contains("[DEV-FJ2R-DEBUG]");
    }

    public void log(@Nullable Object message, Object... args) {
        if (!debug) return;
        if (message instanceof String text) {
            messages.sendToConsole(text, args);
            return;
        }
        messages.sendToConsole(String.valueOf(message), args);
    }

    public void debug(@Nullable Object message, Object... args) {
        log(tag + " - " + message, args);
    }

    /** Debug-gated warning; silent whenever the debug flag is off, so behaviour is unchanged. */
    public void warn(@Nullable Object message, Object... args) {
        if (!debug) return;
        messages.sendToConsole("&e" + tag + " - WARN - " + message, args);
    }

    /** True when the debug flag is on; guard expensive argument building with it. */
    public boolean isDebugEnabled() {
        return debug;
    }

    /** One-line item description for logs; never touches meta or NBT. */
    public static @NotNull String item(@Nullable ItemStack stack) {
        return stack == null ? "null" : stack.getType().name() + "x" + stack.getAmount();
    }

}
