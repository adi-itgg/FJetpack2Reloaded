package me.phantomx.fjetpack.two.reloaded.v2.config.processor;

import org.bukkit.configuration.ConfigurationSection;

public interface ConfigProcessor<T> {

    boolean support(Class<?> clazz, T value);

    Object process(ConfigurationSection section, Class<?> clazz, T value);

}
