package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ProtectionGuard;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.item.ItemTagCodec;
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
public class PlayerRegistry {

    private final Map<UUID, FlightSession> players = new WeakHashMap<>();

    private final JavaPlugin plugin;
    private final YamlConfigRepository configs;
    private final MessageSink messages;
    private final ItemTagCodec itemTags;
    private final ProtectionGuard protection;

    public PlayerRegistry(@NotNull JavaPlugin plugin,
                          @NotNull YamlConfigRepository configs,
                          @NotNull MessageSink messages,
                          @NotNull ItemTagCodec itemTags,
                          @NotNull ProtectionGuard protection) {
        this.plugin = plugin;
        this.configs = configs;
        this.messages = messages;
        this.itemTags = itemTags;
        this.protection = protection;
    }

    /** State of the player, created on first use. */
    public @NotNull FlightSession of(@NotNull Player player) {
        var fj2RPlayer = players.get(player.getUniqueId());
        if (fj2RPlayer == null) {
            fj2RPlayer = new FlightSession(player, this, plugin, configs, messages, itemTags, protection);
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
            fj2RPlayer.turnOff(false, false, false, true);
    }

}
