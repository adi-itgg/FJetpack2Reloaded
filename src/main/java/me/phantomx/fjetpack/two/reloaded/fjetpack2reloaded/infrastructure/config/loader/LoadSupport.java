package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.loader;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.CustomFuel;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Everything a loader needs beside its own file's root section.
 *
 * @param customFuels already loaded custom fuels: a jetpack may reference one by id, so the files
 *                    are loaded in a fixed order
 * @param messages    output port for per entry reporting and warnings
 * @param sender      who asked for the reload
 */
public record LoadSupport(@NotNull Map<String, CustomFuel> customFuels,
                          @NotNull MessageSink messages,
                          @NotNull CommandSender sender) {
}
