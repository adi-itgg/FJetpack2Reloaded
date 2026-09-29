package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Config;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.CustomFuel;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Read access to the loaded configuration.
 * <p>
 * Reloading is deliberately not on the port: only the plugin adapter (the reload command) starts a
 * reload, use cases only read. {@code config()} and {@code message()} are {@code null} until the
 * first successful load, so a caller that runs before startup finished must check.
 */
public interface ConfigRepository {

    @Nullable
    Config config();

    @Nullable
    Message message();

    @NotNull
    Map<String, Jetpack> jetpacks();

    @NotNull
    Map<String, CustomFuel> customFuels();

}
