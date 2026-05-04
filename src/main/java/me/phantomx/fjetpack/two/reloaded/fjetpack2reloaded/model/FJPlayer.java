package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.lang.ref.WeakReference;
import java.util.UUID;

@Data
@Accessors(fluent = true)
public class FJPlayer {

    private WeakReference<Player> player;
    private UUID playerUUID;
    private FuelBurn fuelBurn;

    public FJPlayer(Player player) {
        this.player = new WeakReference<>(player);
        this.playerUUID = player.getUniqueId();
    }

    public FuelBurn fuelBurn() {
        if (this.fuelBurn == null) {
            synchronized (this) {
                this.fuelBurn = new FuelBurn();
            }
        }
        return this.fuelBurn;
    }

    @Data
    @Accessors(fluent = true)
    public static class FuelBurn {

        private boolean active;
        private BukkitTask task;

    }

}
