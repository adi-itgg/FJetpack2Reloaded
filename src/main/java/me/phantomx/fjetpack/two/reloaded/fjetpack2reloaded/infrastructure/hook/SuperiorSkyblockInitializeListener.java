package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook;

import com.bgsoftware.superiorskyblock.api.events.PluginInitializeEvent;
import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.BukkitListener;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

/** {@link PluginInitializeEvent}: register the island flag and privilege while SuperiorSkyblock2 starts. */
@Singleton
public class SuperiorSkyblockInitializeListener implements BukkitListener {

    private final SuperiorSkyblockFlagRegistration flagRegistration;

    public SuperiorSkyblockInitializeListener(@NotNull SuperiorSkyblockFlagRegistration flagRegistration) {
        this.flagRegistration = flagRegistration;
    }

    @EventHandler
    public void onInitialize(PluginInitializeEvent e) {
        flagRegistration.register();
    }

}
