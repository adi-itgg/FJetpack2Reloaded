package me.phantomx.fjetpack.two.reloaded.v2.config.processor.impl;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.v2.config.processor.ConfigProcessor;
import me.phantomx.fjetpack.two.reloaded.v2.util.MessageUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class ListConfigProcessor implements ConfigProcessor<List> {

    @Override
    public boolean support(Class<?> clazz, List value) {
        return clazz.isAssignableFrom(List.class) && value != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object process(ConfigurationSection section, Class<?> clazz, List value) {
        val newList = new ArrayList<String>(value);
        for (int i = 0; i < value.size(); i++) {
            val v = value.get(i);
            if (v instanceof String text) {
                newList.set(i, MessageUtil.translateColorCodes(text + (text.contains("&") ? "&r" : "")));
            }
        }
        return newList;
    }

}
