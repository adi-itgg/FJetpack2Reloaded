package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.loader;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.StringUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

@SuppressWarnings("rawtypes")
public class ConfigListParser implements ConfigParser<List, List> {

    @Override
    public boolean support(Class<?> type) {
        return type.isAssignableFrom(List.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List parse(Class<?> type, List value, ConfigurationSection section) {
        for (int i = 0; i < value.size(); i++) {
            val v = value.get(i);
            if (v instanceof String str) {
                value.set(i, StringUtil.translateColorCodes(v + (str.contains("&") ? "&r" : "")));
            }
        }
        return value;
    }

}
