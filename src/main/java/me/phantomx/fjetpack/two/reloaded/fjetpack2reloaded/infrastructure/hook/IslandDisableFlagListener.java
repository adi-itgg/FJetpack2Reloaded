package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook;

import com.bgsoftware.superiorskyblock.api.events.IslandDisableFlagEvent;
import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.BukkitListener;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

/** {@link IslandDisableFlagEvent}: the island flag was turned off, so check who is still flying. */
@Singleton
public class IslandDisableFlagListener implements BukkitListener {

    private final IslandFlightRevoker revoker;

    public IslandDisableFlagListener(@NotNull IslandFlightRevoker revoker) {
        this.revoker = revoker;
    }

    @EventHandler
    public void onIslandDisableFlagEvent(@NotNull IslandDisableFlagEvent e) {
        revoker.revokeFor(e.getIsland().getAllPlayersInside());
    }

}
