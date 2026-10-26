package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.flight;

import lombok.Getter;
import lombok.Setter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Jetpack;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * What one player's flight currently is: who is flying, with which jetpack, and the handles the
 * lifecycle needs to stop it again.
 * <p>
 * Data only. Everything that decides whether to fly, charges fuel or talks to the player lives in
 * {@link FlightService} - this type has no dependency on any port, so it cannot call back into a use
 * case.
 */
@Getter
@Setter
public class FlightState {

    private @NotNull Player player;
    private boolean active;
    /** The jetpack in use; null until one was selected and cleared again on turn off. */
    private @Nullable Jetpack jetpack;
    /** The repeating fuel tick, once it has run at least once. */
    private @Nullable BukkitTask burnTask;
    /** Guards {@link FlightService#turnOn()} against a second tick loop while the first is starting. */
    private boolean starting;
    /** Marks the worn item as belonging to this activation; see {@code ItemTags.isActiveJetpack}. */
    private int jetpackUniqueId;

    public FlightState(@NotNull Player player) {
        this.player = player;
    }

}
