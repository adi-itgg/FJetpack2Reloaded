package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.loader;

import io.avaje.inject.Prototype;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

@Prototype
@RequiredArgsConstructor
public class CompositeConfigParser implements ConfigParser<Object, Object> {


    @Override
    public boolean support(Class<?> type) {
        return true;
    }

    @Override
    public Object parse(Class<?> type, Object value, ConfigurationSection section) {
        return null;
    }

}
