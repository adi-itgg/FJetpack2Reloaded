package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.event;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.Configs;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.FJ2RPlayer;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Permissions;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Version;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class OnPlayerJoinLeaveEvent {

    public static void onJoin(@NotNull PlayerJoinEvent e) {
        if (!Configs.getConfig().isUpdateNotification()) return;
        if (!Permissions.hasPermission(e.getPlayer(), "checkupdate")) return;
        Version.checkUpdate(e.getPlayer());
    }

    public static void onLeave(@NotNull PlayerQuitEvent e) {
        FJ2RPlayer.getAsFJ2RPlayer(e.getPlayer()).turnOff();
    }

}
