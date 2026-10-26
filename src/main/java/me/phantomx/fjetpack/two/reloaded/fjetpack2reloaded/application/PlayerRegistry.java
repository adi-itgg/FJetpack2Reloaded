package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.flight.FlightComposition;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.flight.FlightSession;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ItemTags;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Owns the live {@link FlightSession} state of every online player.
 * <p>
 * Keyed weakly by UUID on purpose: entries disappear when a player object is collected.
 */
@Singleton
public class PlayerRegistry {

    private final Map<UUID, FlightSession> players = new WeakHashMap<>();

    private final FlightComposition composition;

    public PlayerRegistry(@NotNull JavaPlugin plugin,
                          @NotNull ConfigRepository configs,
                          @NotNull MessageSink messages,
                          @NotNull ItemTags itemTags,
                          @NotNull ProtectionGuard protection,
                          @NotNull ItemFactory items,
                          @NotNull ServerVersion version) {
        this.composition = new FlightComposition(plugin, configs, messages, itemTags, protection, items, version);
    }

    /** State of the player, created on first use. */
    public @NotNull FlightSession of(@NotNull Player player) {
        var fj2RPlayer = players.get(player.getUniqueId());
        if (fj2RPlayer == null) {
            fj2RPlayer = new FlightSession(player, this, composition);
            players.put(player.getUniqueId(), fj2RPlayer);
        }
        fj2RPlayer.setPlayer(player);
        return fj2RPlayer;
    }

    public void remove(@NotNull Player player) {
        players.remove(player.getUniqueId());
    }

    public @NotNull Collection<FlightSession> all() {
        return players.values();
    }

    public void clear() {
        val copy = players.values().toArray(new FlightSession[0]);
        players.clear();
        for (FlightSession fj2RPlayer : copy)
            fj2RPlayer.turnOffUnloaded();
    }

}
