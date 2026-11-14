package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener;

import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ListenerFailureHandler;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.BukkitListener;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.jetbrains.annotations.NotNull;

/** {@link PlayerToggleSneakEvent}: sneak to toggle the jetpack on or off. */
@Singleton
public class OnPlayerToggleSneakListener implements BukkitListener {

    private final OnPlayerCrouchEvent onCrouch;
    private final ListenerFailureHandler failures;
    private final Log log;

    public OnPlayerToggleSneakListener(@NotNull OnPlayerCrouchEvent onCrouch,
                                       @NotNull ListenerFailureHandler failures,
                                       @NotNull MessageSink messages) {
        this.onCrouch = onCrouch;
        this.failures = failures;
        this.log = new Log(getClass(), messages);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerToggleSneakEvent(@NotNull PlayerToggleSneakEvent e) {
        log.debug("PlayerToggleSneakEvent: player=%s sneaking=%s cancelled=%s",
                e.getPlayer().getName(), e.isSneaking(), e.isCancelled());
        Catcher.createVoid(() -> onCrouch.onToggleSneak(e))
                .onFailure(error -> {
                    log.debug("PlayerToggleSneakEvent FAILED: player=%s: %s", e.getPlayer().getName(), error);
                    failures.handle(error, e.getPlayer(), e.getEventName());
                });
    }

}
