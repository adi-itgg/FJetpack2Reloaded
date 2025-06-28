package me.phantomx.fjetpack.two.reloaded.v2.config.processor.impl;

import me.phantomx.fjetpack.two.reloaded.v2.config.processor.ConfigProcessor;
import me.phantomx.fjetpack.two.reloaded.v2.util.MessageUtil;
import org.bukkit.configuration.ConfigurationSection;

public class StringColorCodeConfigProcessor implements ConfigProcessor<String> {

    @Override
    public boolean support(Class<?> clazz, String value) {
        return clazz.isAssignableFrom(String.class)
                && value != null
                && MessageUtil.hasColorCode(value);
    }

    @Override
    public Object process(ConfigurationSection section, Class<?> clazz, String value) {
        if (value.equalsIgnoreCase("none") || value.equalsIgnoreCase("null")) {
            return null;
        }
        return MessageUtil.translateColorCodes(value + "&r");
    }

}
