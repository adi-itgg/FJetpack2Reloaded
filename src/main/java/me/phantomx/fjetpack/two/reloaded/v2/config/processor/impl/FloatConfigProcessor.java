package me.phantomx.fjetpack.two.reloaded.v2.config.processor.impl;

import me.phantomx.fjetpack.two.reloaded.v2.config.processor.ConfigProcessor;
import org.bukkit.configuration.ConfigurationSection;

public class FloatConfigProcessor implements ConfigProcessor<Number> {

    @Override
    public boolean support(Class<?> clazz, Number value) {
        return (clazz.isAssignableFrom(Float.class) || clazz.isAssignableFrom(float.class))
                && value != null;
    }

    @Override
    public Object process(ConfigurationSection section, Class<?> clazz, Number value) {
        return value.floatValue();
    }

}
