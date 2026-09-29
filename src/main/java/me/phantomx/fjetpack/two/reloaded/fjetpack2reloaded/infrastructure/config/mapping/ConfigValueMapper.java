package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.mapping;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 * Handles one kind of config value when mapping a YAML section onto a DTO field.
 */
public interface ConfigValueMapper {

    /**
     * @return true when this mapper handled the field, false to let the next one try
     */
    boolean map(@NotNull Object instance, @NotNull Field field, @NotNull Object value, @NotNull MappingContext context) throws Exception;

}
