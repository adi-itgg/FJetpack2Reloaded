package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook;

import com.bgsoftware.superiorskyblock.api.events.PluginInitializeEvent;
import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.BukkitListener;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** {@link PluginInitializeEvent}: register the island flag and privilege while SuperiorSkyblock2 starts. */
@Singleton
public class SuperiorSkyblockInitializeListener implements BukkitListener {

    private static final String EVENT_CLASS = "com.bgsoftware.superiorskyblock.api.events.PluginInitializeEvent";

    private final SuperiorSkyblockFlagRegistration flagRegistration;

    public SuperiorSkyblockInitializeListener(@NotNull SuperiorSkyblockFlagRegistration flagRegistration) {
        this.flagRegistration = flagRegistration;
    }

    @Override
    public @Nullable String unavailableReason() {
        return SuperiorSkyblock.apiClassPresent(EVENT_CLASS)
                ? null
                : "optional plugin " + SuperiorSkyblock.PLUGIN_NAME + " is not installed on this server";
    }

    @EventHandler
    public void onInitialize(PluginInitializeEvent e) {
        flagRegistration.register();
    }

}
