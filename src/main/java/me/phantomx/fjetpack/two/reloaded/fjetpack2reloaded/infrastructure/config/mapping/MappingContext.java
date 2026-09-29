package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.mapping;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.dto.CustomFuel;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * What a {@link ConfigValueMapper} needs beside the field itself.
 *
 * @param section     the section currently being mapped (its name is used by the "#id" placeholder)
 * @param customFuels already loaded custom fuels, for fields that reference one by id
 * @param messages    output port for mapping warnings
 * @param mappers     the composite, so a mapper can recurse into a nested section
 */
public record MappingContext(@NotNull ConfigurationSection section,
                             @NotNull Map<String, CustomFuel> customFuels,
                             @NotNull MessageSink messages,
                             @NotNull ConfigValueMappers mappers) {
}
