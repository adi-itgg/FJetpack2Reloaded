package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener;

import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.JetpackRefill;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.PlayerRegistry;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.IMessageException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.item.ItemTagCodec;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.logging.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util.UpdateChecker;
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

/**
 * Bukkit event entry point: forwards to the handlers and turns exceptions into user feedback.
 */
@Singleton
public class RegisterEvent implements Listener {

    private final Log log;
    private final Messages messages;
    private final YamlConfigRepository configs;

    private final OnPlayerCrouchEvent onCrouch;
    private final OnPlayerToggleFlightEvent onToggleFlight;
    private final OnPlayerDiedEvent onDied;
    private final OnPlayerInventoryClickEvent onInventoryClick;
    private final OnPlayerDamagedEvent onDamaged;
    private final OnPlayerJoinLeaveEvent onJoinLeave;

    public RegisterEvent(@NotNull PlayerRegistry players,
                         @NotNull YamlConfigRepository configs,
                         @NotNull ItemFactory items,
                         @NotNull ItemTagCodec itemTags,
                         @NotNull Messages messages,
                         @NotNull UpdateChecker updateChecker) {
        this.messages = messages;
        this.configs = configs;
        this.log = new Log(getClass(), messages);

        this.onCrouch = new OnPlayerCrouchEvent(players, items);
        this.onToggleFlight = new OnPlayerToggleFlightEvent(players);
        this.onDied = new OnPlayerDiedEvent(players, configs, messages);
        this.onInventoryClick = new OnPlayerInventoryClickEvent(players, itemTags, new JetpackRefill(configs, itemTags), messages);
        this.onDamaged = new OnPlayerDamagedEvent(items);
        this.onJoinLeave = new OnPlayerJoinLeaveEvent(players, configs, updateChecker);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerToggleSneakEvent(@NotNull PlayerToggleSneakEvent e) {
        //log.debug(e.getEventName());
        Catcher.createVoid(() -> {
            onCrouch.onToggleSneak(e);
        }).onFailure(error -> {
            if (error instanceof NoPermissionLvlException) {
                messages.sendMessage(e.getPlayer(), configs.message().getNoPermission());
                return;
            }
            if (error instanceof IMessageException) return;
            messages.sendMessage("ERROR " + e.getEventName() + ": %s", error);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerToggleFly(@NotNull PlayerToggleFlightEvent e) {
        log.debug(e.getEventName());
        Catcher.createVoid(() -> onToggleFlight.onToggle(e));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDied(@NotNull PlayerDeathEvent e) {
        log.debug(e.getEventName());
        Catcher.createVoid(() -> onDied.onDied(e));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInventoryClick(@NotNull InventoryClickEvent e) {
        log.debug(e.getEventName());
        Catcher.createVoid(() -> onInventoryClick.onClick(e)).onFailure(error -> {
            if (error instanceof NoPermissionLvlException) {
                messages.sendMessage(e.getWhoClicked(), configs.message().getNoPermission());
                return;
            }
            if (error instanceof IMessageException) return;
            messages.sendMessage("ERROR " + e.getEventName() + ": %s", error);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamagedEvent(@NotNull EntityDamageEvent e) {
        log.debug(e.getEventName());
        Catcher.createVoid(() -> onDamaged.onDamaged(e));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        log.debug(e.getEventName());
        Catcher.createVoid(() -> onJoinLeave.onJoin(e));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(@NotNull PlayerQuitEvent e) {
        log.debug(e.getEventName());
        Catcher.createVoid(() -> onJoinLeave.onLeave(e));
    }

}
