package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config;

import io.avaje.inject.PostConstruct;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.annotation.SectionPath;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.loader.CompositeConfigParser;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.Config;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.CustomFuel;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.Message;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Placeholder;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.StringUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
@Singleton
@Accessors(fluent = true)
@RequiredArgsConstructor
public class FJConfig {

    private final CompositeConfigParser parser;
    private final YamlConfiguration yamlConfiguration;
    private final JavaPlugin plugin;
    private final Logger log;


    private @Getter Config config;
    private @Getter Message message;
    private final @Getter Map<String, CustomFuel> customFuels = new HashMap<>();
    private final @Getter Map<String, Jetpack> jetpacks = new HashMap<>();
    private @Getter List<String> helpText;

    @SuppressWarnings("DataFlowIssue")
    @PostConstruct
    void init() {
        try (val br = new BufferedReader(new InputStreamReader(plugin.getResource("help.txt")))) {
            this.helpText = br.lines()
                    .map(s -> s.replace(Placeholder.VERSION, plugin.getDescription().getVersion()))
                    .toList();
        } catch (Exception e) {
            log.error("Failed to load help.txt", e);
        }
    }


    private void loadConfig(String filename) {
        val file = new File(plugin.getDataFolder(), filename);

        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            plugin.saveResource(filename, false);
        }

        try {
            yamlConfiguration.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            log.error("Cannot load configuration file {}", filename, e);
            throw new RuntimeException(e);
        }
    }

    private <T> T load(Class<T> clazz) {
        return load(clazz, null);
    }

    @SneakyThrows
    private <T> T load(Class<T> clazz, ConfigurationSection cfgSection) {
        val instance = (T) clazz.getDeclaredConstructors()[0].newInstance();
        val section = cfgSection != null ? cfgSection : yamlConfiguration;

        for (Field field : clazz.getDeclaredFields()) {
            if (!field.trySetAccessible()) {
                continue;
            }

            // try to get value from field name
            var rawValue = section.get(field.getName());
            if (rawValue == null) {
                // try to get value from field name with first letter capitalized
                val capitalizedFieldName = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
                rawValue = section.get(capitalizedFieldName);
                if (rawValue == null) {
                    // try to get value from kebab case field name
                    val kebabCaseFieldName = StringUtil.toKebabCase(field.getName());
                    rawValue = section.get(kebabCaseFieldName);
                    if (rawValue == null) {
                        continue;
                    }
                }
            }

            // parse value
            val type = field.getType();
            val parsedValue = parser.parse(type, rawValue, section);
            field.set(instance, parsedValue);

            // nested config
            val annotation = type.getDeclaredAnnotation(SectionPath.class);
            if (annotation != null) {
                field.set(instance, load(type, section.getConfigurationSection(annotation.value())));
            }

            // TODO register permission
        }

        return instance;
    }


    public void reloadConfig(CommandSender sender) {
        // load config
        loadConfig("config.yml");
        this.config = load(Config.class);
        log.info("Loaded config version {}", this.config.getVersion());
        Messages.sendMessage(false, sender, "&6Loaded config version &a{}", this.config.getVersion());

        // load message
        loadConfig("message.yml");
        this.message = load(Message.class);
        log.info("Loaded message config");
        Messages.sendMessage(false, sender, "&6Loaded message config");

        // load custom fuels
        loadConfig("custom_fuel.yml");
        this.customFuels.clear();
        for (String id : yamlConfiguration.getKeys(false)) {
            try {
                val customFuel = load(CustomFuel.class, yamlConfiguration.getConfigurationSection(id));
                customFuel.setId(id);
                this.customFuels.put(id, customFuel);
            } catch (Exception e) {
                log.error("Failed to load custom fuel {}", id, e);
            }
        }
        log.info("Loaded {} custom fuels", this.customFuels.size());
        Messages.sendMessage(false, sender, "&6Loaded {} custom fuels", this.customFuels.size());

        // load jetpacks
        loadConfig("jetpack.yml");
        this.jetpacks.clear();
        for (String id : yamlConfiguration.getKeys(false)) {
            try {
                val jetpack = load(Jetpack.class, yamlConfiguration.getConfigurationSection(id));
                jetpack.setId(id);
                this.jetpacks.put(id, jetpack);
            } catch (Exception e) {
                log.error("Failed to load jetpack {}", id, e);
            }
        }
        log.info("Loaded {} jetpacks", this.jetpacks.size());
        Messages.sendMessage(false, sender, "&6Loaded {} jetpacks", this.jetpacks.size());

    }

}
