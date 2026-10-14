package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.message;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The sink runs headless: no server, just a recording sender. It is the last stop before chat, so
 * what it does with a missing key and with the format arguments is worth pinning down.
 */
@DisplayName("Messages")
class MessagesTest {

    private final List<String> sent = new ArrayList<>();
    private final CommandSender sender = recordingSender(sent);

    @Test
    @DisplayName("a message that is null or blank is not sent at all")
    void missingKeyIsSilentInsteadOfFatal() {
        var messages = new Messages();

        messages.sendMessage(sender, null);
        messages.sendMessage(sender, "");
        messages.send(sender, null, "ignored");

        assertTrue(sent.isEmpty(), sent.toString());
    }

    @Test
    @DisplayName("the configured prefix is prepended and color codes are translated")
    void prefixAndColorAreApplied() {
        var messages = new Messages();
        messages.usePrefix("&c[FJ2R]");

        messages.sendMessage(sender, "&aHi");

        assertEquals(List.of(ChatColor.translateAlternateColorCodes('&', "&c[FJ2R] &aHi")), sent);
    }

    @Test
    @DisplayName("a message can opt out of the prefix")
    void prefixCanBeSkipped() {
        var messages = new Messages();
        messages.usePrefix("&c[FJ2R]");

        messages.sendMessage(false, sender, "plain");

        assertEquals(List.of("plain"), sent);
    }

    @Test
    @DisplayName("format arguments fill the placeholders")
    void formatArgumentsAreApplied() {
        var messages = new Messages();

        messages.sendMessage(false, sender, "%s has %d", "VIP", 3);

        assertEquals(List.of("VIP has 3"), sent);
    }

    @Test
    @DisplayName("a throwable argument becomes its stack trace, an array becomes a list")
    void specialArgumentsAreConverted() {
        var messages = new Messages();

        messages.sendMessage(false, sender, "boom: %s", new IllegalStateException("kaput"));
        messages.sendMessage(false, sender, "args: %s", (Object) new String[]{"a", "b"});

        assertTrue(sent.get(0).contains("IllegalStateException"), sent.get(0));
        assertTrue(sent.get(0).contains("kaput"), sent.get(0));
        assertEquals("args: [a, b]", sent.get(1));
    }

    /** A CommandSender that only implements what the sink uses. */
    private static CommandSender recordingSender(List<String> received) {
        return (CommandSender) Proxy.newProxyInstance(
                MessagesTest.class.getClassLoader(),
                new Class<?>[]{CommandSender.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "sendMessage" -> {
                        received.add(String.valueOf(args[0]));
                        yield null;
                    }
                    case "getName" -> "tester";
                    case "toString" -> "CommandSender(tester)";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                });
    }

}
