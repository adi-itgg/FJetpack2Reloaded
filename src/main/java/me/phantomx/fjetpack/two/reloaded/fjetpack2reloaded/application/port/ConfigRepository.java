package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Config;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.CustomFuel;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Message;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Read access to the loaded configuration.
 * <p>
 * Reloading is deliberately not on the port: only the plugin adapter (the reload command) starts a
 * reload, use cases only read.
 * <p>
 * Nothing here returns null. A file that is missing or broken leaves the previous value in place -
 * the built in defaults on the very first load - so a use case never has to check for an
 * uninitialised repository. What that means per field: an unconfigured message key reads as the
 * empty string, which {@link MessageSink#send} turns into silence.
 */
public interface ConfigRepository {

    @NotNull
    Config config();

    @NotNull
    Message message();

    @NotNull
    Map<String, Jetpack> jetpacks();

    @NotNull
    Map<String, CustomFuel> customFuels();

}
