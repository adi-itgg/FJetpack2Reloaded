package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.mapping;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight.ItemDisposition;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.SectionPath;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.CustomFuel;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The config contract: YAML keys map onto DTO fields, by reflection. Runs headless - the only mapper
 * that needs a live server is the permission one (it registers the node on the server), so that one
 * is not covered here.
 */
class ConfigValueMappersTest {

    private final ConfigValueMappers mappers = ConfigValueMappers.defaults();
    private final Warnings warnings = new Warnings();

    @Test
    void mapsEveryValueKindOfASection() throws Exception {
        CustomFuel cvip = customFuel("CVIP");

        var yaml = new YamlConfiguration();
        yaml.set("Item", "CHAINMAIL_CHESTPLATE");
        yaml.set("OnDeath", "drop");
        yaml.set("DisplayName", "&aJetpack");
        yaml.set("Plain", "no codes here");
        yaml.set("Lore", new ArrayList<>(List.of("&bLine", "plain line")));
        yaml.set("Speed", 1.1D);
        yaml.set("CustomFuel", "CVIP");
        yaml.set("Amount", 42);
        yaml.set("Fuel.Cost", 7);
        yaml.set("Fuel.CustomFuel", "CVIP");

        Spec spec = mappers.mapSection(yaml.getConfigurationSection(""), Spec.class, Map.of("CVIP", cvip), warnings);

        assertEquals(Material.CHAINMAIL_CHESTPLATE, spec.item);
        assertEquals(ItemDisposition.DROP, spec.onDeath, "enum names are matched case insensitively");
        assertEquals(ChatColor.translateAlternateColorCodes('&', "&aJetpack&r"), spec.displayName);
        assertEquals("no codes here", spec.plain);
        assertEquals(List.of(ChatColor.translateAlternateColorCodes('&', "&bLine&r"), "plain line"), spec.lore);
        assertEquals(1.1f, spec.speed);
        assertEquals(42, spec.amount);
        assertEquals(cvip, spec.customFuel);
        assertEquals(7, spec.fuel.cost, "nested section via @SectionPath");
        assertEquals(cvip, spec.fuel.customFuel);
        assertTrue(warnings.isEmpty(), "no unexpected warning");
    }

    @Test
    void noneDisablesTheCustomFuelField() throws Exception {
        var yaml = new YamlConfiguration();
        yaml.set("CustomFuel", "NONE");

        Spec spec = mappers.mapSection(yaml.getConfigurationSection(""), Spec.class, Map.of(), warnings);

        assertNull(spec.customFuel);
        assertTrue(warnings.isEmpty());
    }

    @Test
    void anUnknownCustomFuelIdWarnsAndLeavesTheFieldNull() throws Exception {
        var yaml = new YamlConfiguration();
        yaml.set("CustomFuel", "MISSING");

        Spec spec = mappers.mapSection(yaml.getConfigurationSection(""), Spec.class, Map.of(), warnings);

        assertNull(spec.customFuel);
        assertEquals(1, warnings.size());
    }

    @Test
    void absentKeysKeepTheJavaDefaults() throws Exception {
        var yaml = new YamlConfiguration();
        yaml.set("Item", "COAL");

        Spec spec = mappers.mapSection(yaml.getConfigurationSection(""), Spec.class, Map.of(), warnings);

        assertEquals(Material.COAL, spec.item);
        assertNull(spec.displayName);
        assertEquals(0, spec.amount);
        assertEquals(5, spec.fuel.cost, "untouched nested default of the fixture");
    }

    @Test
    void keysMayStartWithALowerCaseLetter() throws Exception {
        var yaml = new YamlConfiguration();
        yaml.set("amount", 3);

        Spec spec = mappers.mapSection(yaml.getConfigurationSection(""), Spec.class, Map.of(), warnings);

        assertEquals(3, spec.amount);
    }

    @Test
    void nullSectionMapsToNull() throws Exception {
        assertNull(mappers.mapSection(null, Spec.class, Map.of(), warnings));
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

    /** Records infrastructure output instead of writing it to a server console. */
    private static final class Warnings implements MessageSink {

        private final List<String> messages = new ArrayList<>();

        @Override
        public void send(CommandSender target, String message, Object... args) {
            messages.add(String.format(message, args));
        }

        @Override
        public void sendToConsole(String message, Object... args) {
            messages.add(String.format(message, args));
        }

        
        public void usePrefix(String prefix) {
            // this test does not check the prefix
        }

        boolean isEmpty() {
            return messages.isEmpty();
        }

        int size() {
            return messages.size();
        }

    }

    /** Fixture mirroring the shapes of the real DTOs, without the permission field. */
    public static class Spec {
        private Material item;
        private ItemDisposition onDeath;
        private String displayName;
        private String plain;
        private List<String> lore;
        private Float speed;
        private CustomFuel customFuel;
        private int amount;
        private Fuel fuel = new Fuel();
    }

    @SectionPath("Fuel")
    public static class Fuel {
        private int cost = 5;
        private CustomFuel customFuel;
    }

}
