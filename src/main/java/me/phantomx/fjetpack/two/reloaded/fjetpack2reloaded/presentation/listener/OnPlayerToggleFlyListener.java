package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener;

import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ListenerFailureHandler;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.BukkitListener;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.jetbrains.annotations.NotNull;

/** {@link PlayerToggleFlightEvent}: keep the flight state honest once the jetpack is gone. */
@Singleton
public class OnPlayerToggleFlyListener implements BukkitListener {

    private final OnPlayerToggleFlightEvent onToggleFlight;
    private final ListenerFailureHandler failures;
    private final Log log;

    public OnPlayerToggleFlyListener(@NotNull OnPlayerToggleFlightEvent onToggleFlight,
                                     @NotNull ListenerFailureHandler failures,
                                     @NotNull MessageSink messages) {
        this.onToggleFlight = onToggleFlight;
        this.failures = failures;
        this.log = new Log(getClass(), messages);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerToggleFly(@NotNull PlayerToggleFlightEvent e) {
        log.debug("PlayerToggleFlightEvent: player=%s flying=%s cancelled=%s",
                e.getPlayer().getName(), e.isFlying(), e.isCancelled());
        Catcher.createVoid(() -> onToggleFlight.onToggle(e))
                .onFailure(error -> failures.handle(error, e.getPlayer(), e.getEventName()));
    }

}
