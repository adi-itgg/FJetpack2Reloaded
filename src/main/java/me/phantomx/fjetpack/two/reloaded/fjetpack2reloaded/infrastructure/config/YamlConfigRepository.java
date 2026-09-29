package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.dto.Config;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.dto.CustomFuel;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.dto.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.dto.Message;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader.ConfigLoader;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader.CustomFuelConfigLoader;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader.JetpackConfigLoader;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader.LoadSupport;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader.MessageConfigLoader;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader.PluginConfigLoader;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.logging.Log;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Owns every YAML file of the plugin: it reads the files, runs the {@link ConfigLoader}s in order
 * and keeps the loaded state.
 * <p>
 * Field names of the DTOs are the YAML keys - do not rename them.
 */
@Singleton
public class YamlConfigRepository {

    private final JavaPlugin plugin;
    private final MessageSink messages;
    private final Log log;

    private final PluginConfigLoader pluginConfigLoader;
    private final MessageConfigLoader messageConfigLoader;
    private final CustomFuelConfigLoader customFuelLoader;
    private final JetpackConfigLoader jetpackLoader;

    private Config config;
    private Message message;
    private Map<String, Jetpack> jetpacks = Collections.emptyMap();
    private Map<String, CustomFuel> customFuels = Collections.emptyMap();

    public YamlConfigRepository(@NotNull JavaPlugin plugin,
                                @NotNull MessageSink messages,
                                @NotNull PluginConfigLoader pluginConfigLoader,
                                @NotNull MessageConfigLoader messageConfigLoader,
                                @NotNull CustomFuelConfigLoader customFuelLoader,
                                @NotNull JetpackConfigLoader jetpackLoader) {
        this.plugin = plugin;
        this.messages = messages;
        this.pluginConfigLoader = pluginConfigLoader;
        this.messageConfigLoader = messageConfigLoader;
        this.customFuelLoader = customFuelLoader;
        this.jetpackLoader = jetpackLoader;
        this.log = new Log(getClass(), messages);
    }

    public void reload() {
        reload(Bukkit.getServer().getConsoleSender());
    }

    /**
     * Fixed order on purpose: a jetpack may reference a custom fuel by id, and the message prefix
     * becomes available while loading message.yml.
     */
    public void reload(@NotNull CommandSender sender) {
        reloadSingle(sender, pluginConfigLoader, loaded -> config = loaded);
        reloadSingle(sender, messageConfigLoader, loaded -> {
            message = loaded;
            // an empty message.yml must not replace the built in prefix with "null"
            if (loaded != null && loaded.getPrefix() != null) messages.usePrefix(loaded.getPrefix());
        });
        reloadEntries(sender, customFuelLoader, loaded -> customFuels = loaded);
        reloadEntries(sender, jetpackLoader, loaded -> jetpacks = loaded);
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

    private <T> void reloadSingle(@NotNull CommandSender sender,
                                  @NotNull ConfigLoader.Single<T> loader,
                                  @NotNull Consumer<T> apply) {
        Catcher.create(() -> loader.load(read(loader.file()), support(sender)))
                .onSuccess(loaded -> {
                    apply.accept(loaded);
                    messages.send(sender, "Loaded &6%s&a", loader.file());
                })
                .onFailure(error -> messages.send(sender, "&cError while load %s", loader.file()));
    }

    private <T> void reloadEntries(@NotNull CommandSender sender,
                                   @NotNull ConfigLoader.Entries<T> loader,
                                   @NotNull Consumer<Map<String, T>> apply) {
        Catcher.create(() -> loader.load(read(loader.file()), support(sender)))
                .onSuccess(loaded -> {
                    apply.accept(loaded);
                    messages.send(sender, "Loaded &6%s&a", loader.file());
                })
                .onFailure(error -> messages.send(sender, "&cError while load %s", loader.file()));
    }

    private @NotNull LoadSupport support(@NotNull CommandSender sender) {
        return new LoadSupport(customFuels, messages, sender);
    }

    /**
     * Reads a file, creating it from the jar when it is missing. The {@link YamlConfiguration} itself
     * is handed to the loaders: it acts as the root section and is never null, unlike
     * {@code getConfigurationSection("")} on an empty file.
     */
    private @NotNull YamlConfiguration read(@NotNull String file) {
        val target = new File(plugin.getDataFolder(), file);
        if (!target.exists()) {
            //noinspection ResultOfMethodCallIgnored
            target.getParentFile().mkdirs();
            plugin.saveResource(file, false);
        }
        return YamlConfiguration.loadConfiguration(target);
    }

}
