package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc;

import io.avaje.inject.Component;
import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.model.FJPlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class FJPlayerManager {

    private final Map<UUID, FJPlayer> players = new ConcurrentHashMap<>();

    private final Server server;
    private final Plugin plugin;

    public FJPlayer getOrCreate(Player player) {
        return players.computeIfAbsent(player.getUniqueId(), id -> new FJPlayer(player));
    }

    public void cleanup(UUID id) {
        var fjPlayer = players.get(id);
        if (fjPlayer == null) {
            return;
        }
        deactivate(fjPlayer);
        players.remove(id);
    }

    public void activate(Player player, Jetpack jetpack, ItemStack item) {
        activate(getOrCreate(player), jetpack, item, EquipmentSlot.OFF_HAND);
    }

    public void activate(FJPlayer fjPlayer, Jetpack jetpack, ItemStack item, EquipmentSlot equipmentSlot) {
        if (fjPlayer == null) {
            throw new IllegalArgumentException("player must be not null!");
        }
        if (jetpack == null) {
            throw new IllegalArgumentException("Jetpack must be not null");
        }
        if (item == null) {
            throw new IllegalArgumentException("jetpack item must be not null!");
        }
        if (equipmentSlot == null) {
            throw new IllegalArgumentException("EquipmentSlot must be not null!");
        }

        var delayInSec = jetpack.getFuel().getBurnRate();
        var fuelBurn = fjPlayer.fuelBurn();
        fuelBurn.active(true);

        server.getScheduler().runTaskTimerAsynchronously(plugin, (bukkitTask) -> {



        }, 0L, delayInSec * 20L);
    }

    public void deactivate(FJPlayer fjPlayer) {

    }
}
