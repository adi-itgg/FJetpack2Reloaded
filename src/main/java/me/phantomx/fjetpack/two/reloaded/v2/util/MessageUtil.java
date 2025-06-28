package me.phantomx.fjetpack.two.reloaded.v2.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

@UtilityClass
public final class MessageUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");

    public static boolean hasColorCode(@NotNull String message) {
        return HEX_PATTERN.matcher(message).find();
    }

    /**
     * Translates color codes to Minecraft color codes.
     * <p>
     * It supports both the old <code>&#x00A7;</code> format and the new <code>#hex</code> format.
     * <p>
     *
     * @param message The message to translate
     * @return The translated message
     */
    public static @NotNull String translateColorCodes(@NotNull String message) {
        var matcher = HEX_PATTERN.matcher(message);
        while (matcher.find()) {
            var hexCode = message.substring(matcher.start(), matcher.end());
            var replaceSharp = hexCode.replace('#', 'x');

            val ch = replaceSharp.toCharArray();
            val builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = HEX_PATTERN.matcher(message);
        }
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message;
    }

    public static void sendMessage(@Nullable CommandSender sender, @Nullable String message) {
        if (sender == null || message == null || message.isEmpty()) {
            return;
        }
        sender.sendMessage(translateColorCodes(message));
    }


}
