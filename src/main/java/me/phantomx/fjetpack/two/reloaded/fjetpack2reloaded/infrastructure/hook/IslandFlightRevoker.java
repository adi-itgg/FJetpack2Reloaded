package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.PlayerRegistry;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Ends the flight of every active flyer on an island that no longer allows it.
 * <p>
 * Shared by the two island events, which carry the same payload and need the same response.
 */
@Singleton
public class IslandFlightRevoker {

    private final PlayerRegistry players;
    private final ConfigRepository configs;
    private final MessageSink messages;

    public IslandFlightRevoker(@NotNull PlayerRegistry players,
                               @NotNull ConfigRepository configs,
                               @NotNull MessageSink messages) {
        this.players = players;
        this.configs = configs;
        this.messages = messages;
    }

    public void revokeFor(@NotNull List<SuperiorPlayer> islandPlayers) {
        islandPlayers.forEach(superiorPlayer -> {
            val player = superiorPlayer.asPlayer();
            if (player == null) return;
            val fj2RPlayer = players.of(player);
            if (!fj2RPlayer.isActive()) return;
            if (fj2RPlayer.isAllowedSuperiorSkyblock()) return;
            fj2RPlayer.turnOff();
            messages.send(player, configs.message().getSuperiorSkyblock2NoPermission());
        });
    }

}
