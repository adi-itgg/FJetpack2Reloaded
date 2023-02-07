package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.event;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.Configs;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.logging.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.jetbrains.annotations.NotNull;

public class RegisterEvent implements Listener {

    private final Log log = new Log(this.getClass());

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerToggleSneakEvent(@NotNull PlayerToggleSneakEvent e) {
        //log.debug(e.getEventName());
        Catcher.createVoid(() -> {
            OnPlayerCrouchEvent.onToggleSneak(e);
        }).onFailure(error -> {
            if (error instanceof NoPermissionLvlException) {
                Messages.sendMessage(e.getPlayer(), Configs.getMessage().getNoPermission());
                return;
            }
            Messages.sendMessage("ERROR " + e.getEventName() + ": %s", error);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerToggleFly(@NotNull PlayerToggleFlightEvent e) {
        log.debug(e.getEventName());
        Catcher.createVoid(() -> OnPlayerToggleFlightEvent.onToggle(e));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDied(@NotNull PlayerDeathEvent e) {
        log.debug(e.getEventName());
        Catcher.createVoid(() -> OnPlayerDiedEvent.onDied(e));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInventoryClick(@NotNull InventoryClickEvent e) {
        log.debug(e.getEventName());
        Catcher.createVoid(() -> OnPlayerInventoryClickEvent.onClick(e)).onFailure(error -> {
            if (error instanceof NoPermissionLvlException) {
                Messages.sendMessage(e.getWhoClicked(), Configs.getMessage().getNoPermission());
                return;
            }
            Messages.sendMessage("ERROR " + e.getEventName() + ": %s", error);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamagedEvent(@NotNull EntityDamageEvent e) {
        log.debug(e.getEventName());
        Catcher.createVoid(() -> OnPlayerDamagedEvent.onDamaged(e));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        log.debug(e.getEventName());
        Catcher.createVoid(() -> OnPlayerJoinLeaveEvent.onJoin(e));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(@NotNull PlayerQuitEvent e) {
        log.debug(e.getEventName());
        Catcher.createVoid(() -> OnPlayerJoinLeaveEvent.onLeave(e));
    }

}
