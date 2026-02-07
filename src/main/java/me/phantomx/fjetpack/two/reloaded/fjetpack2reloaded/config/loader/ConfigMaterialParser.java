package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.loader;

import io.avaje.inject.Prototype;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

@Prototype
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
