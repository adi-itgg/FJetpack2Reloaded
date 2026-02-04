package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.loader;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class ConfigMaterialParser implements ConfigParser<String, Material> {

    @Override
    public boolean support(Class<?> type) {
        return type.isAssignableFrom(Material.class);
    }

    @Override
    public Material parse(Class<?> type, String value, ConfigurationSection section) {
        return Material.valueOf(value.toUpperCase());
    }

}
