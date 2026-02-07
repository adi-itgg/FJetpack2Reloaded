package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.loader;

import io.avaje.inject.Prototype;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.StringUtil;
import org.bukkit.configuration.ConfigurationSection;

@Prototype
public class ConfigStringParser implements ConfigParser<String, String> {
    @Override
    public boolean support(Class<?> type) {
        return type.isAssignableFrom(String.class);
    }

    @Override
    public String parse(Class<?> type, String value, ConfigurationSection section) {
        return StringUtil.translateColorCodes(value);
    }
}
