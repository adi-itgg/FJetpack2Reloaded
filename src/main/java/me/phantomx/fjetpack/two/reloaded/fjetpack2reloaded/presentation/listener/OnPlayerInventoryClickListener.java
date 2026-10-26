package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener;

import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ListenerFailureHandler;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.BukkitListener;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * {@link InventoryClickEvent}: drop the flight when the worn jetpack is clicked away, and refuel when
 * a fuel item is clicked onto it.
 */
@Singleton
public class OnPlayerInventoryClickListener implements BukkitListener {

    private final OnPlayerInventoryClickEvent onClick;
    private final ListenerFailureHandler failures;
    private final Log log;

    public OnPlayerInventoryClickListener(@NotNull OnPlayerInventoryClickEvent onClick,
                                          @NotNull ListenerFailureHandler failures,
                                          @NotNull MessageSink messages) {
        this.onClick = onClick;
        this.failures = failures;
        this.log = new Log(getClass(), messages);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInventoryClick(@NotNull InventoryClickEvent e) {
        log.debug(e.getEventName());
        Catcher.createVoid(() -> onClick.onClick(e))
                .onFailure(error -> failures.handle(error, e.getWhoClicked(), e.getEventName()));
    }

}
