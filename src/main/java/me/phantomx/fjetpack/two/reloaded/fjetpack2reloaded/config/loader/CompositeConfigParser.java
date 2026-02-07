package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.loader;

import io.avaje.inject.PostConstruct;
import io.avaje.inject.Prototype;
import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.di.PluginFactory;
import org.bukkit.configuration.ConfigurationSection;

@Prototype
@RequiredArgsConstructor
@SuppressWarnings({"rawtypes", "unchecked"})
public class CompositeConfigParser implements ConfigParser<Object, Object> {

    private ConfigParser[] parsers;

    private final PluginFactory.Provider provider;

    @PostConstruct
    void init() {
        this.parsers = this.provider.provideList(ConfigParser.class)
                .stream()
                .filter(o -> !(o instanceof CompositeConfigParser))
                .toArray(ConfigParser[]::new);
    }


    @Override
    public boolean support(Class<?> type) {
        return true;
    }

    @Override
    public Object parse(Class<?> type, Object value, ConfigurationSection section) {
        for (ConfigParser parser : this.parsers) {
            if (parser.support(type)) {
                value = parser.parse(type, value, section);
            }
        }
        return value;
    }

}
