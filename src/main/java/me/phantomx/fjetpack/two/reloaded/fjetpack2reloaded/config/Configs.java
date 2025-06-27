package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config;

import lombok.*;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.FJetpack2Reloaded;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.Pair;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.Config;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.CustomFuel;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.Message;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.logging.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Nothing;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Configs {

    private static final String configYaml = "config.yml";
    private static final String jetpackYaml = "jetpack.yml";
    private static final String customFuelYaml = "custom_fuel.yml";
    private static final String messageYaml = "message.yml";


    @Setter(AccessLevel.PRIVATE)
    @Getter
    private static @NonNull Config config;
    @Setter(AccessLevel.PRIVATE)
    @Getter
    private static @NotNull Map<String, Jetpack> jetpacksLoaded = Collections.emptyMap();
    @Setter(AccessLevel.PRIVATE)
    @Getter
    private static @NotNull Map<String, CustomFuel> customFuelLoaded = Collections.emptyMap();
    @Setter(AccessLevel.PRIVATE)
    @Getter
    private static @NonNull Message message;

    @Contract("_ -> new")
    private static @NotNull Pair<YamlConfiguration, ConfigurationSection> loadConfig(@NotNull String filename) {
        val plugin = FJetpack2Reloaded.getPlugin();
        val file = new File(plugin.getDataFolder(), filename);

        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            plugin.saveResource(filename, false);
        }

        val configYaml = YamlConfiguration.loadConfiguration(file);
        val rootSection = configYaml.getConfigurationSection("");
        return new Pair<>(configYaml, rootSection);
    }

    public static void reloadConfig() {
        reloadConfig(Bukkit.getServer().getConsoleSender());
    }

    public static void reloadConfig(@NotNull CommandSender sender) {
        loadConfigYaml()
                .onSuccess(result -> Messages.sendMessage(sender, "Loaded &6%s&a", configYaml))
                .onFailure(error -> Messages.sendMessage(sender, "&cError while load %s", configYaml));
        loadMessageConfig()
                .onSuccess(result -> Messages.sendMessage(sender, "Loaded &6%s&a", messageYaml))
                .onFailure(error -> Messages.sendMessage(sender, "&cError while load %s", messageYaml));
        loadCustomFuel(sender)
                .onSuccess(result -> Messages.sendMessage(sender, "Loaded &6%s&a", customFuelYaml))
                .onFailure(error -> Messages.sendMessage(sender, "&cError while load %s", customFuelYaml));
        loadJetpackConfig(sender)
                .onSuccess(result -> Messages.sendMessage(sender, "Loaded &6%s&a", jetpackYaml))
                .onFailure(error -> Messages.sendMessage(sender, "&cError while load %s", jetpackYaml));
    }


    private static @NotNull Catcher<Nothing> loadConfigYaml() {
        return Catcher.createVoid(() -> {
            val config = loadConfig(configYaml);
            assert config.getSecond() != null;

            val conf = ConfigsLoader.appendConfigValue(config.getSecond(), Config.class);
            assert conf != null;
            setConfig(conf);
        });
    }
    @SneakyThrows
    private static @NotNull Catcher<Nothing> loadJetpackConfig(@NotNull CommandSender sender) {
        return Catcher.createVoid(() -> {
            val plugin = FJetpack2Reloaded.getPlugin();
            val file = new File(plugin.getDataFolder(), jetpackYaml);

            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
                plugin.saveResource(jetpackYaml, false);
            }

            val config = loadConfig(jetpackYaml);
            assert config.getSecond() != null;
            val jetpackList = new HashMap<String, Jetpack>();

            for (String id : config.getSecond().getKeys(false))
                Catcher.create(() -> {
                    val jetpack = ConfigsLoader.appendConfigValue(config.getFirst().getConfigurationSection(id), Jetpack.class);
                    assert jetpack != null;
                    jetpack.setId(id);
                    return jetpack;
                }).onSuccess(jetpack -> {
                    jetpack.setDisplayName(Messages.translateColorCodes(jetpack.getDisplayName()));
                    jetpack.setLore(jetpack.getLore().stream().map(Messages::translateColorCodes).toList());
                    jetpackList.put(jetpack.getId(), jetpack);
                    Log.log("&aLoaded Jetpack:&r %s", jetpack);
                    Messages.sendMessage(sender, "&aLoaded jetpack: &l%s", id);
                }).onFailure(error -> {
                    Log.log("&cFailure load jetpack: %s - error: %s", id, error);
                    Messages.sendMessage(sender, "&cFailure load jetpack: %s", id);
                });

            setJetpacksLoaded(jetpackList);
        });
    }

    private static @NotNull Catcher<Nothing> loadCustomFuel(@NotNull CommandSender sender) {
        return Catcher.createVoid(() -> {
           val config = loadConfig(customFuelYaml);
           assert config.getSecond() != null;

            val loadedCustomFuels = new HashMap<String, CustomFuel>();
            for (String id : config.getFirst().getKeys(false)) {
                Catcher.create(() -> {
                    val customFuel = ConfigsLoader.appendConfigValue(config.getFirst().getConfigurationSection(id), CustomFuel.class);
                    assert customFuel != null;
                    customFuel.setId(id);
                    customFuel.setDisplayName(Messages.translateColorCodes(customFuel.getDisplayName()));
                    customFuel.setLore(customFuel.getLore().stream().map(Messages::translateColorCodes).toList());
                    return customFuel;
                }).onSuccess(customFuel -> {
                    loadedCustomFuels.put(customFuel.getId(), customFuel);
                    Log.log("&aLoaded custom fuel: &l%s", customFuel.toString());
                    Messages.sendMessage(sender, "&aLoaded custom fuel: &l%s", id);
                }).onFailure(error -> Log.log("&cFailed load custom fuel: %s - details: %s", id, error));
            }
           setCustomFuelLoaded(loadedCustomFuels);
        });
    }

    private static @NotNull Catcher<Nothing> loadMessageConfig() {
        return Catcher.createVoid(() -> {
            val config = loadConfig(messageYaml);
            assert config.getSecond() != null;

            val message = ConfigsLoader.appendConfigValue(config.getSecond(), Message.class);
            assert message != null;

            setMessage(message);
            Log.log("&aLoaded config %s - %s", messageYaml, message);
        }).onFailure(error -> Log.log("&cLoad message.yml error: %s", error));
    }

}
