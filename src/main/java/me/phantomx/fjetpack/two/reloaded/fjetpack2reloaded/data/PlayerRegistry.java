package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemTagCodec;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.MessageSink;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Owns the live {@link FJ2RPlayer} state of every online player.
 * <p>
 * Keyed weakly by UUID on purpose: entries disappear when a player object is collected.
 */
public class PlayerRegistry {

    private final Map<UUID, FJ2RPlayer> players = new WeakHashMap<>();

    private final JavaPlugin plugin;
    private final YamlConfigRepository configs;
    private final MessageSink messages;
    private final ItemTagCodec itemTags;

    public PlayerRegistry(@NotNull JavaPlugin plugin,
                          @NotNull YamlConfigRepository configs,
                          @NotNull MessageSink messages,
                          @NotNull ItemTagCodec itemTags) {
        this.plugin = plugin;
        this.configs = configs;
        this.messages = messages;
        this.itemTags = itemTags;
    }

    /** State of the player, created on first use. */
    public @NotNull FJ2RPlayer of(@NotNull Player player) {
        var fj2RPlayer = players.get(player.getUniqueId());
        if (fj2RPlayer == null) {
            fj2RPlayer = new FJ2RPlayer(player, this, plugin, configs, messages, itemTags);
            players.put(player.getUniqueId(), fj2RPlayer);
        }
        fj2RPlayer.setPlayer(player);
        return fj2RPlayer;
    }

    public void remove(@NotNull Player player) {
        players.remove(player.getUniqueId());
    }

    public @NotNull Collection<FJ2RPlayer> all() {
        return players.values();
    }

    public void clear() {
        val copy = players.values().toArray(new FJ2RPlayer[0]);
        players.clear();
        for (FJ2RPlayer fj2RPlayer : copy)
            fj2RPlayer.turnOff(false, false, false, true);
    }

}
