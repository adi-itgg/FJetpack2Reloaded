package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config;

import lombok.SneakyThrows;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.dto.Config;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.dto.CustomFuel;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.dto.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.dto.Message;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Nothing;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.logging.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.message.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and owns every YAML file of the plugin.
 * <p>
 * Field names of the {@code data.config} DTOs are the YAML keys - do not rename them.
 */
public class YamlConfigRepository {

    private static final String CONFIG_YAML = "config.yml";
    private static final String JETPACK_YAML = "jetpack.yml";
    private static final String CUSTOM_FUEL_YAML = "custom_fuel.yml";
    private static final String MESSAGE_YAML = "message.yml";

    private final JavaPlugin plugin;
    private final MessageSink messages;
    private final Log log;

    private Config config;
    private Message message;
    private Map<String, Jetpack> jetpacks = Collections.emptyMap();
    private Map<String, CustomFuel> customFuels = Collections.emptyMap();

    public YamlConfigRepository(@NotNull JavaPlugin plugin, @NotNull MessageSink messages) {
        this.plugin = plugin;
        this.messages = messages;
        this.log = new Log(getClass(), messages);
    }

    public void reload() {
        reload(Bukkit.getServer().getConsoleSender());
    }

    public void reload(@NotNull CommandSender sender) {
        loadConfigYaml()
                .onSuccess(result -> messages.send(sender, "Loaded &6%s&a", CONFIG_YAML))
                .onFailure(error -> messages.send(sender, "&cError while load %s", CONFIG_YAML));
        loadMessageConfig()
                .onSuccess(result -> messages.send(sender, "Loaded &6%s&a", MESSAGE_YAML))
                .onFailure(error -> messages.send(sender, "&cError while load %s", MESSAGE_YAML));
        loadCustomFuel(sender)
                .onSuccess(result -> messages.send(sender, "Loaded &6%s&a", CUSTOM_FUEL_YAML))
                .onFailure(error -> messages.send(sender, "&cError while load %s", CUSTOM_FUEL_YAML));
        loadJetpackConfig(sender)
                .onSuccess(result -> messages.send(sender, "Loaded &6%s&a", JETPACK_YAML))
                .onFailure(error -> messages.send(sender, "&cError while load %s", JETPACK_YAML));
    }

    public Config config() {
        return config;
    }

    public Message message() {
        return message;
    }

    public @NotNull Map<String, Jetpack> jetpacks() {
        return jetpacks;
    }

    public @NotNull Map<String, CustomFuel> customFuels() {
        return customFuels;
    }

    private @Nullable ConfigurationSection loadConfig(@NotNull String filename) {
        val file = new File(plugin.getDataFolder(), filename);

        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            plugin.saveResource(filename, false);
        }



        return YamlConfiguration.loadConfiguration(file).getConfigurationSection("");
    }

    private @NotNull Catcher<Nothing> loadConfigYaml() {
        return Catcher.createVoid(() -> {
            val section = loadConfig(CONFIG_YAML);
            assert section != null;

            val conf = ConfigsLoader.appendConfigValue(section, Config.class, customFuels, messages);
            assert conf != null;
            config = conf;
        });
    }

    @SneakyThrows
    private @NotNull Catcher<Nothing> loadJetpackConfig(@NotNull CommandSender sender) {
        return Catcher.createVoid(() -> {
            val section = loadConfig(JETPACK_YAML);
            assert section != null;
            val jetpackList = new HashMap<String, Jetpack>();

            for (String id : section.getKeys(false))
                Catcher.create(() -> {
                    val jetpack = ConfigsLoader.appendConfigValue(section.getConfigurationSection(id), Jetpack.class, customFuels, messages);
                    assert jetpack != null;
                    jetpack.setId(id);
                    return jetpack;
                }).onSuccess(jetpack -> {
                    jetpack.setDisplayName(Messages.translateColorCodes(jetpack.getDisplayName()));
                    jetpack.setLore(jetpack.getLore().stream().map(Messages::translateColorCodes).toList());
                    jetpackList.put(jetpack.getId(), jetpack);
                    log.log("&aLoaded Jetpack:&r %s", jetpack);
                    messages.send(sender, "&aLoaded jetpack: &l%s", id);
                }).onFailure(error -> {
                    log.log("&cFailure load jetpack: %s - error: %s", id, error);
                    messages.send(sender, "&cFailure load jetpack: %s", id);
                });

            jetpacks = jetpackList;
        });
    }

    private @NotNull Catcher<Nothing> loadCustomFuel(@NotNull CommandSender sender) {
        return Catcher.createVoid(() -> {
            val section = loadConfig(CUSTOM_FUEL_YAML);
            assert section != null;

            val loadedCustomFuels = new HashMap<String, CustomFuel>();
            for (String id : section.getKeys(false)) {
                Catcher.create(() -> {
                    val customFuel = ConfigsLoader.appendConfigValue(section.getConfigurationSection(id), CustomFuel.class, customFuels, messages);
                    assert customFuel != null;
                    customFuel.setId(id);
                    customFuel.setDisplayName(Messages.translateColorCodes(customFuel.getDisplayName()));
                    customFuel.setLore(customFuel.getLore().stream().map(Messages::translateColorCodes).toList());
                    return customFuel;
                }).onSuccess(customFuel -> {
                    loadedCustomFuels.put(customFuel.getId(), customFuel);
                    log.log("&aLoaded custom fuel: &l%s", customFuel.toString());
                    messages.send(sender, "&aLoaded custom fuel: &l%s", id);
                }).onFailure(error -> log.log("&cFailed load custom fuel: %s - details: %s", id, error));
            }
            customFuels = loadedCustomFuels;
        });
    }

    private @NotNull Catcher<Nothing> loadMessageConfig() {
        return Catcher.createVoid(() -> {
            val section = loadConfig(MESSAGE_YAML);
            assert section != null;

            val loadedMessage = ConfigsLoader.appendConfigValue(section, Message.class, customFuels, messages);
            assert loadedMessage != null;

            message = loadedMessage;
            log.log("&aLoaded config %s - %s", MESSAGE_YAML, loadedMessage);
        }).onFailure(error -> log.log("&cLoad message.yml error: %s", error));
    }

}
