package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.message;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Message;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util.ColorText;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Singleton
public class Messages implements MessageSink {

    private static final String DEFAULT_PREFIX = "&e&l[&bFJetpack&62Reloaded&e&l]&r";

    /** Until message.yml is loaded, and whenever the prefix is not configured. */
    private volatile String prefix = DEFAULT_PREFIX;

    @Override
    public void usePrefix(@NotNull String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void send(@NotNull CommandSender target, @NotNull String message, Object @NotNull ... args) {
        sendMessage(target, message, args);
    }

    public void sendMessage(@NotNull CommandSender target, @NotNull String message, Object @NotNull ... args) {
        sendMessage(true, target, message, args);
    }

    public void sendMessage(boolean withPrefix, @NotNull CommandSender target, @NotNull String message, Object @NotNull ... args) {
        if (message.isEmpty()) return;
        val effectivePrefix = prefix + " ";
        message = ColorText.translate(withPrefix ? effectivePrefix + message : message);
        if (args.length == 0) {
            target.sendMessage(message);
            return;
        }
        val convertedArgs = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof Throwable) {
                convertedArgs.add(ExceptionUtils.getStackTrace((Throwable) arg));
                continue;
            }
            if (arg instanceof String[]) {
                val sb = new StringBuilder("[");
                for (String s : ((String[]) arg)) {
                    if (sb.length() > 2) sb.append(", ");
                    sb.append(s);
                }
                sb.append("]");
                convertedArgs.add(sb.toString());
                continue;
            }
            convertedArgs.add(arg);
        }
        message = String.format(message, convertedArgs.toArray());
        target.sendMessage(message);
    }

    /**
     * Send message to console
     *
     * @param args for string format like %s
     */
    public void sendMessage(@NotNull String message, Object... args) {
        sendMessage(true, Bukkit.getConsoleSender(), message, args);
    }

}
