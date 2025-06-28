package me.phantomx.fjetpack.two.reloaded.v2.config.processor.impl;

import me.phantomx.fjetpack.two.reloaded.v2.config.processor.ConfigProcessor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class MaterialConfigProcessor implements ConfigProcessor<String> {

    @Override
    public boolean support(Class<?> clazz, String value) {
        return clazz.isAssignableFrom(Material.class) && value != null;
    }

    @Override
    public Object process(ConfigurationSection section, Class<?> clazz, String value) {
        return Material.valueOf(value.toUpperCase());
    }

}
