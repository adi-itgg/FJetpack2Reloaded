package me.phantomx.fjetpack.two.reloaded.v2.config.processor.impl;

import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.CustomFuel;
import me.phantomx.fjetpack.two.reloaded.v2.config.processor.ConfigProcessor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

@RequiredArgsConstructor
public class CustomFuelConfigProcessor implements ConfigProcessor<String> {

    private final Map<String, CustomFuel> customFuels;

    @Override
    public boolean support(Class<?> clazz, String value) {
        return clazz.isAssignableFrom(CustomFuel.class) && value != null;
    }

    @Override
    public Object process(ConfigurationSection section, Class<?> clazz, String value) {
        return customFuels.get(value);
    }

}
