package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.dto.Config;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.mapping.ConfigValueMappers;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** config.yml */
@Singleton
public class PluginConfigLoader implements ConfigLoader.Single<Config> {

    private final ConfigValueMappers mappers;

    public PluginConfigLoader(@NotNull ConfigValueMappers mappers) {
        this.mappers = mappers;
    }

    @Override
    public @NotNull String file() {
        return "config.yml";
    }

    @Override
    public @Nullable Config load(@NotNull ConfigurationSection root, @NotNull LoadSupport support) throws Exception {
        return mappers.mapSection(root, Config.class, support.customFuels(), support.messages());
    }

}
