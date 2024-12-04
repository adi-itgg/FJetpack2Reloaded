package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.Configs;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Version;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Messages {

    public static final String DEFAULT_PREFIX = "&e&l[&bFJetpack&62Reloaded&e&l]&r";

    public static @NotNull String translateColorCodes(@NotNull String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message;
    }


    public static void sendMessage(@NotNull CommandSender target, @NotNull String message, Object @NotNull ... args) {
        sendMessage(true, target, message, args);
    }
    public static void sendMessage(boolean withPrefix, @NotNull CommandSender target, @NotNull String message, Object @NotNull ... args) {
        if (message.isEmpty()) return;
        val prefix = Catcher.create(() -> Configs.getMessage().getPrefix()).getOrDefault(DEFAULT_PREFIX) + " ";
        message = translateColorCodes(withPrefix ? prefix + message : message);
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
    public static void sendMessage(@NotNull String message, Object... args) {
        sendMessage(true, Bukkit.getConsoleSender(), message, args);
    }

}
