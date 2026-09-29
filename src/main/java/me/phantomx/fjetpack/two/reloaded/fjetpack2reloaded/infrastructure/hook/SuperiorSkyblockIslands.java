package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.Island;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.IslandLookup;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** SuperiorSkyblock2 adapter for {@link IslandLookup}. */
@Singleton
public class SuperiorSkyblockIslands implements IslandLookup {

    @Override
    public boolean active() {
        return SuperiorSkyblock.isActive();
    }

    @NotNull
    @Override
    public String privilegeName() {
        return SuperiorSkyblock.FLAG_PRIVILEGE;
    }

    @Override
    public @Nullable Island islandAt(@NotNull Player player) {
        val island = SuperiorSkyblockAPI.getIslandAt(player.getLocation());
        return island == null ? null : new WrappedIsland(island);
    }

    private record WrappedIsland(@NotNull com.bgsoftware.superiorskyblock.api.island.Island island) implements Island {

        @Override
        public @NotNull UUID id() {
            return island.getUniqueId();
        }

        @Override
        public boolean flagEnabled(@NotNull String flag) {
            return island.hasSettingsEnabled(IslandFlag.getByName(flag));
        }

        @Override
        public boolean hasPrivilege(@NotNull Player player, @NotNull String privilege) {
            return island.hasPermission(player, IslandPrivilege.getByName(privilege));
        }

    }

}
