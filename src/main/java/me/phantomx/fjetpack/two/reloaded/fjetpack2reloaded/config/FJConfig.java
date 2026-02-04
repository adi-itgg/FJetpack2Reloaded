package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.Pair;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

@Singleton
@RequiredArgsConstructor
public class FJConfig {

    private Pair<YamlConfiguration, ConfigurationSection> loadConfig(String filename) {

    }

}
