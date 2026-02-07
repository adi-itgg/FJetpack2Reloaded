package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.loader;

import io.avaje.inject.Prototype;
import org.bukkit.configuration.ConfigurationSection;

@Prototype
public class ConfigFloatParser implements ConfigParser<Number, Float> {

    @Override
    public boolean support(Class<?> type) {
        return type.isAssignableFrom(Float.class);
    }

    @Override
    public Float parse(Class<?> type, Number value, ConfigurationSection section) {
        return value.floatValue();
    }

}
