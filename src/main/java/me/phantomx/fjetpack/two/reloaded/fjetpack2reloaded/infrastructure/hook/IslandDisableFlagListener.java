package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook;

import com.bgsoftware.superiorskyblock.api.events.IslandDisableFlagEvent;
import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.BukkitListener;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** {@link IslandDisableFlagEvent}: the island flag was turned off, so check who is still flying. */
@Singleton
public class IslandDisableFlagListener implements BukkitListener {

    private static final String EVENT_CLASS = "com.bgsoftware.superiorskyblock.api.events.IslandDisableFlagEvent";

    private final IslandFlightRevoker revoker;

    public IslandDisableFlagListener(@NotNull IslandFlightRevoker revoker) {
        this.revoker = revoker;
    }

    @Override
    public @Nullable String unavailableReason() {
        return SuperiorSkyblock.apiClassPresent(EVENT_CLASS)
                ? null
                : "optional plugin " + SuperiorSkyblock.PLUGIN_NAME + " is not installed on this server";
    }

    @EventHandler
    public void onIslandDisableFlagEvent(@NotNull IslandDisableFlagEvent e) {
        revoker.revokeFor(e.getIsland().getAllPlayersInside());
    }

}
