package me.phantomx.fjetpack.two.reloaded.v2.config;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.annotation.SectionPath;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.Config;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.CustomFuel;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.Message;
import me.phantomx.fjetpack.two.reloaded.v2.config.processor.ConfigProcessor;
import me.phantomx.fjetpack.two.reloaded.v2.config.processor.impl.*;
import me.phantomx.fjetpack.two.reloaded.v2.util.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Getter
@Accessors(fluent = true)
public class ConfigManager {

    private static final String CONFIG_YAML = "config.yml";
    private static final String JETPACK_YAML = "jetpack.yml";
    private static final String CUSTOM_FUEL_YAML = "custom_fuel.yml";
    private static final String MESSAGE_YAML = "message.yml";

    private final JavaPlugin plugin;
    private final ConfigProcessor<Object> configProcessor;

    private Config config;
    private Map<String, Jetpack> jetpacks = new HashMap<>();
    private Map<String, CustomFuel> customFuels = new HashMap<>();
    private Message message;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;

        val configProcessors = new ArrayList<ConfigProcessor<?>>();

        configProcessors.add(new EnumConfigProcessor());
        configProcessors.add(new FloatConfigProcessor());
        configProcessors.add(new ListConfigProcessor());
        configProcessors.add(new MaterialConfigProcessor());
        configProcessors.add(new PermissionConfigProcessor(plugin.getServer().getPluginManager()));
        configProcessors.add(new StringColorCodeConfigProcessor());
        configProcessors.add(new CustomFuelConfigProcessor(customFuels));

        this.configProcessor = new CompositeConfigProcessor(configProcessors);
    }


    public void loadAllConfig(CommandSender sender) {
        loadConfig(sender);
        loadMessageConfig(sender);
        loadCustomFuelConfig(sender);
        loadJetpackConfig(sender);
    }

    private void loadConfig(CommandSender sender) {
        try {
            this.config = appendConfig(loadConfig(CONFIG_YAML).getConfigurationSection(""), Config.class);
            MessageUtil.sendMessage(sender, "&aLoaded config.yml");
        } catch (Throwable e) {
            plugin.getLogger().warning("Failed to load config: " + e.getMessage());
        }
    }

    private void loadJetpackConfig(CommandSender sender) {
        try {
            val section = loadConfig(JETPACK_YAML).getConfigurationSection("");
            if (section == null) {
                plugin.getLogger().warning("Failed to load jetpack config because the root section is null");
                return;
            }
            this.jetpacks.clear();
            for (String id : section.getKeys(false)) {
                try {
                    val jetpack = appendConfig(section.getConfigurationSection(id), Jetpack.class);
                    if (jetpack == null) {
                        plugin.getLogger().warning("Failed to load jetpack: " + id);
                        continue;
                    }
                    jetpack.setId(id);
                    this.jetpacks.put(id, jetpack);
                    MessageUtil.sendMessage(sender, "&aLoaded jetpack: &l" + id);
                } catch (Throwable e) {
                    val errorMsg = "&cFailed to load jetpack: " + id + " - Error: " + e.getMessage();
                    plugin.getLogger().warning(errorMsg);
                    MessageUtil.sendMessage(sender, errorMsg);
                }
            }
        } catch (Throwable e) {
            plugin.getLogger().warning("Failed to load jetpack config: " + e.getMessage());
        }
    }

    private void loadCustomFuelConfig(CommandSender sender) {
        try {
            val section = loadConfig(CUSTOM_FUEL_YAML).getConfigurationSection("");
            if (section == null) {
                plugin.getLogger().warning("Failed to load custom fuel config because the root section is null");
                return;
            }
            this.customFuels.clear();
            for (String id : section.getKeys(false)) {
                try {
                    val customFuel = appendConfig(section.getConfigurationSection(id), CustomFuel.class);
                    if (customFuel == null) {
                        plugin.getLogger().warning("Failed to load custom fuel: " + id);
                        continue;
                    }
                    customFuel.setId(id);
                    this.customFuels.put(id, customFuel);
                    MessageUtil.sendMessage(sender, "&aLoaded custom fuel: &l" + id);
                } catch (Throwable e) {
                    val errorMsg = "&cFailed to load custom fuel: " + id + " - Error: " + e.getMessage();
                    plugin.getLogger().warning(errorMsg);
                    MessageUtil.sendMessage(sender, errorMsg);
                }
            }
        } catch (Throwable e) {
            plugin.getLogger().warning("Failed to load custom fuel config: " + e.getMessage());
        }
    }


    private void loadMessageConfig(CommandSender sender) {
        try {
            this.message = appendConfig(loadConfig(MESSAGE_YAML).getConfigurationSection(""), Message.class);
            MessageUtil.sendMessage(sender, "&aLoaded message.yml");
        } catch (Throwable e) {
            plugin.getLogger().warning("Failed to load message config: " + e.getMessage());
        }
    }


    private YamlConfiguration loadConfig(String filename) throws Throwable {
        val file = new File(plugin.getDataFolder(), filename);

        if (!file.exists()) {
            // if the directory doesn't exist, create it
            if (file.getParentFile() != null && !file.getParentFile().mkdirs()) {
                plugin.getLogger().warning("Failed to create directory: " + file.getParentFile().getAbsolutePath());
            }
            // if the file doesn't exist, create it
            plugin.saveResource(filename, false);
        }

        // load the config
        return YamlConfiguration.loadConfiguration(file);
    }

    @SuppressWarnings("unchecked")
    private <T> @Nullable T appendConfig(ConfigurationSection section, Class<T> configClass) throws Throwable {
        if (section == null) {
            plugin.getLogger().warning("Failed to load config because the root section is null: " + configClass.getSimpleName());
            return null;
        }

        // new config instance
        val config = (T) configClass.getDeclaredConstructors()[0].newInstance();
        for (Field field : configClass.getDeclaredFields()) {
            // skip if the field cannot be set
            if (!field.trySetAccessible()) {
                continue;
            }

            var value = section.get(field.getName());
            if (value == null) { // try to get the value from the key
                var key = field.getName();
                key = key.substring(0, 1).toUpperCase() + key.substring(1); // capitalize the first letter
                value = section.get(key);
            }
            if (value == null) {
                continue; // skip if the value is null
            }

            val type = field.getType();

            // process the value
            if (configProcessor.support(type, value)) {
                val processedValue = configProcessor.process(section, type, value);
                if (processedValue != null) {
                    field.set(config, processedValue);
                }
            }

            // if the field has a section
            val annotation = type.getDeclaredAnnotation(SectionPath.class);
            if (annotation != null) {
                field.set(config, appendConfig(section.getConfigurationSection(annotation.value()), type));
            }

        }

        return config;
    }

    public void reloadConfig(@NotNull CommandSender sender) {
        loadAllConfig(sender);
    }
}
