package me.phantomx.fjetpack.two.reloaded.v2.config.processor.impl;

import me.phantomx.fjetpack.two.reloaded.v2.config.processor.ConfigProcessor;
import org.bukkit.configuration.ConfigurationSection;

public class EnumConfigProcessor implements ConfigProcessor<String> {

    @Override
    public boolean support(Class<?> clazz, String value) {
        return clazz.isEnum() && value != null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Object process(ConfigurationSection section, Class<?> clazz, String value) {
        return Enum.valueOf((Class<? extends Enum>) clazz, value.toUpperCase());
    }

}
