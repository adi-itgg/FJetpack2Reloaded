package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.CustomFuel;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.mapping.ConfigValueMappers;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The loaders run headless: YAML plus a recording sender, no server. They are what turns a file into
 * DTOs and what reports each entry.
 */
class ConfigLoadersTest {

    private final ConfigValueMappers mappers = ConfigValueMappers.defaults();
    private final Recording sink = new Recording();
    private final List<String> received = new ArrayList<>();
    private final CommandSender sender = recordingSender(received);
    private final LoadSupport support = new LoadSupport(Map.of(), sink, sender);

    @Test
    void jetpacksAreLoadedPerIdAndBrokenEntriesAreSkipped() {
        CustomFuel cvip = customFuel("CVIP");

        var yaml = new YamlConfiguration();
        yaml.set("VIP.Item", "CHAINMAIL_CHESTPLATE");
        yaml.set("VIP.DisplayName", "&bVIP");
        yaml.set("VIP.Lore", new ArrayList<>(List.of("&7line")));
        yaml.set("VIP.Fuel.Cost", 3);
        yaml.set("VIP.Fuel.CustomFuel", "CVIP");
        yaml.set("BROKEN.Item", "NOT_A_MATERIAL");

        var loaded = new JetpackConfigLoader(mappers, sink)
                .load(yaml, new LoadSupport(Map.of("CVIP", cvip), sink, sender));

        assertEquals(List.of("VIP"), List.copyOf(loaded.keySet()), "the broken entry is skipped");
        assertEquals(ChatColor.translateAlternateColorCodes('&', "&bVIP&r"), loaded.get("VIP").getDisplayName(), "color codes are translated by the mapper");
        assertEquals(List.of(ChatColor.translateAlternateColorCodes('&', "&7line&r")), loaded.get("VIP").getLore());
        assertEquals(3, loaded.get("VIP").getFuel().getCost());
        assertSame(cvip, loaded.get("VIP").getFuel().getCustomFuel(), "custom fuel referenced by id");

        assertTrue(received.contains("&aLoaded jetpack: &lVIP"), received.toString());
        assertTrue(received.contains("&cFailure load jetpack: BROKEN"), received.toString());
    }

    @Test
    void customFuelsAreLoadedPerIdAndAFailureOnlyGoesToTheLog() {
        var yaml = new YamlConfiguration();
        yaml.set("CVIP.Item", "COAL");
        yaml.set("CVIP.DisplayName", "&6CVIP");
        yaml.set("CVIP.Lore", new ArrayList<>(List.of("lore")));
        yaml.set("BROKEN.Item", "NOT_A_MATERIAL");

        var loaded = new CustomFuelConfigLoader(mappers, sink).load(yaml, support);

        assertEquals(List.of("CVIP"), List.copyOf(loaded.keySet()));
        assertEquals("CVIP", loaded.get("CVIP").getId(), "the id comes from the key");
        assertEquals(ChatColor.translateAlternateColorCodes('&', "&6CVIP&r"), loaded.get("CVIP").getDisplayName());
        assertEquals(List.of("&aLoaded custom fuel: &lCVIP"), received, "a broken fuel is logged, not reported to the sender");
    }

    @Test
    void thePluginConfigIsASingleObject() throws Exception {
        var yaml = new YamlConfiguration();
        yaml.set("version", 2);
        yaml.set("UpdateNotification", false);

        var loaded = new PluginConfigLoader(mappers).load(yaml, support);

        assertEquals(2, loaded.getVersion());
        assertFalse(loaded.isUpdateNotification());
        assertTrue(received.isEmpty(), "a single object file reports nothing per entry");
    }

    private static CustomFuel customFuel(String id) {
        var customFuel = new CustomFuel();
        customFuel.setId(id);
        customFuel.setCustomDisplay("CVIP");
        customFuel.setDisplayName("&6CVIP");
        customFuel.setLore(List.of("lore"));
        customFuel.setItem(Material.COAL);
        return customFuel;
    }

    /** Stands in for Messages: delivers to the target like the real sink does. */
    private static final class Recording implements MessageSink {

        private final List<String> console = new ArrayList<>();

        @Override
        public void send(CommandSender target, String message, Object... args) {
            target.sendMessage(String.format(message, args));
        }

        @Override
        public void sendToConsole(String message, Object... args) {
            console.add(String.format(message, args));
        }

        @Override
        public void usePrefix(String prefix) {
            // not used by the loaders
        }

    }

    /** A CommandSender that only implements what the loaders use. */
    private static CommandSender recordingSender(List<String> received) {
        return (CommandSender) Proxy.newProxyInstance(
                ConfigLoadersTest.class.getClassLoader(),
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
