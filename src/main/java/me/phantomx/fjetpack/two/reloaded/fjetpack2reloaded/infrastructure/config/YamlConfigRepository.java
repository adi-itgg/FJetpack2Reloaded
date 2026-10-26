package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Config;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.CustomFuel;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Message;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader.ConfigLoader;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader.CustomFuelConfigLoader;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader.JetpackConfigLoader;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader.LoadSupport;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader.MessageConfigLoader;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader.PluginConfigLoader;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Owns every YAML file of the plugin: it reads the files, runs the {@link ConfigLoader}s in order
 * and keeps the loaded state.
 * <p>
 * Field names of the DTOs are the YAML keys - do not rename them.
 */
@Singleton
public class YamlConfigRepository implements ConfigRepository {

    private final JavaPlugin plugin;
    private final MessageSink messages;
    private final Log log;

    private final PluginConfigLoader pluginConfigLoader;
    private final MessageConfigLoader messageConfigLoader;
    private final CustomFuelConfigLoader customFuelLoader;
    private final JetpackConfigLoader jetpackLoader;

    /** Seeded, never null: a file that fails to load must not leave the repository unreadable. */
    private Config config = new Config();
    private Message message = new Message();
    private Map<String, Jetpack> jetpacks = Collections.emptyMap();
    private Map<String, CustomFuel> customFuels = Collections.emptyMap();

    /** Files the last load could not read, reported to whoever asked for the reload. */
    private List<String> failedFiles = List.of();

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
        val failed = new ArrayList<String>();
        reloadSingle(sender, failed, pluginConfigLoader, this::useLoadedConfig);
        reloadSingle(sender, failed, messageConfigLoader, this::useLoadedMessage);
        reloadEntries(sender, failed, customFuelLoader, loaded -> customFuels = loaded);
        reloadEntries(sender, failed, jetpackLoader, loaded -> jetpacks = loaded);
        this.failedFiles = List.copyOf(failed);
    }

    /**
     * Files the last load could not read. Empty when everything was fine.
     * <p>
     * A reload keeps serving whatever was already in use, so the caller decides what a broken file
     * means: with no jetpack loaded there is nothing to fly with, and the plugin says so instead of
     * running as a no-op.
     */
    public @NotNull List<String> failedFiles() {
        return failedFiles;
    }

    @Override
    public @NotNull Config config() {
        return config;
    }

    @Override
    public @NotNull Message message() {
        return message;
    }

    /**
     * A failed or empty file keeps whatever is already in use - the built in default on the very
     * first load. Replacing a working config with null is how a broken file used to take the
     * plugin down later, at the first message it tried to send.
     */
    private void useLoadedConfig(@Nullable Config loaded) {
        if (loaded != null) config = loaded;
    }

    private void useLoadedMessage(@Nullable Message loaded) {
        if (loaded == null) return;
        message = loaded;
        // an empty prefix means "not configured": keep the built in one instead of blanking it
        if (!loaded.getPrefix().isEmpty()) messages.usePrefix(loaded.getPrefix());
    }

    @Override
    public @NotNull Map<String, Jetpack> jetpacks() {
        return jetpacks;
    }

    @Override
    public @NotNull Map<String, CustomFuel> customFuels() {
        return customFuels;
    }

    private <T> void reloadSingle(@NotNull CommandSender sender,
                                  @NotNull List<String> failed,
                                  @NotNull ConfigLoader.Single<T> loader,
                                  @NotNull Consumer<T> apply) {
        Catcher.create(() -> loader.load(read(loader.file()), support(sender)))
                .onSuccess(loaded -> {
                    apply.accept(loaded);
                    messages.send(sender, "Loaded &6%s&a", loader.file());
                })
                .onFailure(error -> report(sender, failed, loader.file(), error));
    }

    private <T> void reloadEntries(@NotNull CommandSender sender,
                                   @NotNull List<String> failed,
                                   @NotNull ConfigLoader.Entries<T> loader,
                                   @NotNull Consumer<Map<String, T>> apply) {
        Catcher.create(() -> loader.load(read(loader.file()), support(sender)))
                .onSuccess(loaded -> {
                    apply.accept(loaded);
                    messages.send(sender, "Loaded &6%s&a", loader.file());
                })
                .onFailure(error -> report(sender, failed, loader.file(), error));
    }

    /** A file that could not be read is both reported now and remembered for the caller. */
    private void report(@NotNull CommandSender sender,
                        @NotNull List<String> failed,
                        @NotNull String file,
                        @NotNull Throwable error) {
        failed.add(file);
        messages.send(sender, "&cError while load %s: %s", file, error);
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
