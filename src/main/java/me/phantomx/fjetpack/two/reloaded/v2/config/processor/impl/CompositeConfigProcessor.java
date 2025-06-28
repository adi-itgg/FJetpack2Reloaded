package me.phantomx.fjetpack.two.reloaded.v2.config.processor.impl;

import me.phantomx.fjetpack.two.reloaded.v2.config.processor.ConfigProcessor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class CompositeConfigProcessor implements ConfigProcessor<Object> {

    private final ConfigProcessor[] processors;

    public CompositeConfigProcessor(List<ConfigProcessor<?>> processors) {
        this.processors = processors.toArray(new ConfigProcessor[0]);
    }

    @Override
    public boolean support(Class<?> clazz, Object value) {
        return Arrays.stream(processors).anyMatch(processor -> processor.support(clazz, value));
    }

    @Override
    public Object process(ConfigurationSection section, Class<?> clazz, Object value) {
        for (ConfigProcessor processor : processors) {
            if (processor.support(clazz, value)) {
                return processor.process(section, clazz, value);
            }
        }

        return value;
    }

}
