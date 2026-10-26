package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener;

import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ListenerFailureHandler;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.BukkitListener;
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

    public OnPlayerToggleSneakListener(@NotNull OnPlayerCrouchEvent onCrouch,
                                       @NotNull ListenerFailureHandler failures) {
        this.onCrouch = onCrouch;
        this.failures = failures;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerToggleSneakEvent(@NotNull PlayerToggleSneakEvent e) {
        Catcher.createVoid(() -> onCrouch.onToggleSneak(e))
                .onFailure(error -> failures.handle(error, e.getPlayer(), e.getEventName()));
    }

}
