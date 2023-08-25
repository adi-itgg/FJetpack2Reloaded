package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.model;

import lombok.*;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.Jetpack;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Data
@Setter(AccessLevel.PROTECTED)
public class NewFJ2RPlayer {

    private boolean isActive;
    private @NonNull Player player;
    private @Nullable Jetpack jetpack;
    private @Nullable BukkitTask burnTask;
    private @Nullable BukkitTask particleTask;
    private @Nullable UUID superiorSkyblock2IslandUUID;
    private @Nullable Claim griefPreventionClaim;
    private boolean starting;
    private int uuid;

}
