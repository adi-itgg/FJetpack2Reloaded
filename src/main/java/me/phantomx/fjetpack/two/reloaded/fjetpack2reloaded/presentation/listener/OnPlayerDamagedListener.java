package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener;

import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.BukkitListener;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

/** {@link EntityDamageEvent}: keep an unbreakable jetpack at zero damage on old servers. */
@Singleton
public class OnPlayerDamagedListener implements BukkitListener {

    private final OnPlayerDamagedEvent onDamaged;
    private final Log log;

    public OnPlayerDamagedListener(@NotNull OnPlayerDamagedEvent onDamaged, @NotNull MessageSink messages) {
        this.onDamaged = onDamaged;
        this.log = new Log(getClass(), messages);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamagedEvent(@NotNull EntityDamageEvent e) {
        log.debug(e.getEventName());
        Catcher.createVoid(() -> onDamaged.onDamaged(e));
    }

}
