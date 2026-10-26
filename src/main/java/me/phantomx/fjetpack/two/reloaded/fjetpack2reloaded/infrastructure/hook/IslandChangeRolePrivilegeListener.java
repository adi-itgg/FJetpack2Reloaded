package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook;

import com.bgsoftware.superiorskyblock.api.events.IslandChangeRolePrivilegeEvent;
import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.BukkitListener;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

/** {@link IslandChangeRolePrivilegeEvent}: an island role lost a privilege, so check who is still flying. */
@Singleton
public class IslandChangeRolePrivilegeListener implements BukkitListener {

    private final IslandFlightRevoker revoker;

    public IslandChangeRolePrivilegeListener(@NotNull IslandFlightRevoker revoker) {
        this.revoker = revoker;
    }

    @EventHandler
    public void onIslandChangeRolePrivilegeEvent(@NotNull IslandChangeRolePrivilegeEvent e) {
        revoker.revokeFor(e.getIsland().getAllPlayersInside());
    }

}
