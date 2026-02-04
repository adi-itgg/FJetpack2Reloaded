package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.loader;

import org.bukkit.configuration.ConfigurationSection;


public class ConfigEnumParser implements ConfigParser<String, Enum<?>> {

    @Override
    public boolean support(Class<?> type) {
        return type.isEnum();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enum<?> parse(
            Class<?> type, String value, ConfigurationSection section) {
        return Enum.valueOf((Class<? extends Enum>) type, value.toUpperCase());
    }

}
