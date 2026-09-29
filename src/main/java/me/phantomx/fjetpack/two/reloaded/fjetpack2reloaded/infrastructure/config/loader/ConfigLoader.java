package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Loads one config file of the plugin.
 * <p>
 * {@code T} is the entry type of that file: the DTO itself for a file holding a single object, one
 * entry of the map for a file keyed by id. Field names of the DTOs are the YAML keys.
 */
public interface ConfigLoader<T> {

    /** File name inside the plugin data folder, also used in the load report. */
    @NotNull
    String file();

    /** A file holding one object. */
    interface Single<T> extends ConfigLoader<T> {

        /** @return null when the section could not be mapped at all */
        @Nullable T load(@NotNull ConfigurationSection root, @NotNull LoadSupport support) throws Exception;

    }

    /** A file holding {@code id -> entry}, in the order the loader decided. */
    interface Entries<T> extends ConfigLoader<T> {

        @NotNull Map<String, T> load(@NotNull ConfigurationSection root, @NotNull LoadSupport support) throws Exception;

    }

}
