package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener;

import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.BukkitListener;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

/** {@link PlayerDeathEvent}: apply the jetpack's {@code OnDeath} disposition. */
@Singleton
public class OnPlayerDeathListener implements BukkitListener {

    private final OnPlayerDiedEvent onDied;
    private final Log log;

    public OnPlayerDeathListener(@NotNull OnPlayerDiedEvent onDied, @NotNull MessageSink messages) {
        this.onDied = onDied;
        this.log = new Log(getClass(), messages);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDied(@NotNull PlayerDeathEvent e) {
        log.debug(e.getEventName());
        Catcher.createVoid(() -> onDied.onDied(e));
    }

}
