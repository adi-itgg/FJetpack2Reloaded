package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.loader;

import org.bukkit.configuration.ConfigurationSection;

public interface ConfigParser<T, V> {

    boolean support(Class<?> type);

    V parse(Class<?> type, T value, ConfigurationSection section);

}
