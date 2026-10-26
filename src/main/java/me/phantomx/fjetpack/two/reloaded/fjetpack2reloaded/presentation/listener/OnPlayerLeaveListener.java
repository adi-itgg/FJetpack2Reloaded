package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener;

import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.BukkitListener;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/** {@link PlayerQuitEvent}: end the flight session, so it is not left active for the next join. */
@Singleton
public class OnPlayerLeaveListener implements BukkitListener {

    private final OnPlayerJoinLeaveEvent onJoinLeave;
    private final Log log;

    public OnPlayerLeaveListener(@NotNull OnPlayerJoinLeaveEvent onJoinLeave, @NotNull MessageSink messages) {
        this.onJoinLeave = onJoinLeave;
        this.log = new Log(getClass(), messages);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(@NotNull PlayerQuitEvent e) {
        log.debug(e.getEventName());
        Catcher.createVoid(() -> onJoinLeave.onLeave(e));
    }

}
