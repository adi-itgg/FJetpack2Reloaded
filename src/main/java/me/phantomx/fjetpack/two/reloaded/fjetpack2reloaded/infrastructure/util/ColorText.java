package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Pure color code translation: hex {@code #rrggbb} plus legacy {@code &} codes. */
public final class ColorText {

    private static final Pattern HEX = Pattern.compile("#[a-fA-F0-9]{6}");

    private ColorText() {
    }

    public static @NotNull String translate(@NotNull String message) {
        Matcher matcher = HEX.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = HEX.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
