package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.PlayerRegistry;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Permissions;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util.UpdateChecker;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class OnPlayerJoinLeaveEvent {

    private final PlayerRegistry players;
    private final YamlConfigRepository configs;
    private final UpdateChecker updateChecker;

    public OnPlayerJoinLeaveEvent(@NotNull PlayerRegistry players,
                                  @NotNull YamlConfigRepository configs,
                                  @NotNull UpdateChecker updateChecker) {
        this.players = players;
        this.configs = configs;
        this.updateChecker = updateChecker;
    }

    public void onJoin(@NotNull PlayerJoinEvent e) {
        if (!configs.config().isUpdateNotification()) return;
        if (!Permissions.hasPermission(e.getPlayer(), "checkupdate")) return;
        updateChecker.check(e.getPlayer());
    }

    public void onLeave(@NotNull PlayerQuitEvent e) {
        players.of(e.getPlayer()).turnOff();
    }

}
