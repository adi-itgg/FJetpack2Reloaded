package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.hook.event;

import com.bgsoftware.superiorskyblock.api.events.IslandChangeRolePrivilegeEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandDisableFlagEvent;
import com.bgsoftware.superiorskyblock.api.events.PluginInitializeEvent;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.FJetpack2Reloaded;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.Configs;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.FJ2RPlayer;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.hook.SuperiorSkyblock;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SuperiorSkyblockEvent implements Listener {

    private boolean isRegistered = false;

    public SuperiorSkyblockEvent() {
        if (Bukkit.getServer().getPluginManager().isPluginEnabled(SuperiorSkyblock.PLUGIN_NAME))
            Messages.sendMessage("&aRegistering %s...", SuperiorSkyblock.PLUGIN_NAME);
        Bukkit.getScheduler().runTaskLater(FJetpack2Reloaded.getPlugin(), this::checkIsHooked, 3 * 20L);
    }

    private void checkIsHooked() {
        if (!Bukkit.getServer().getPluginManager().isPluginEnabled(SuperiorSkyblock.PLUGIN_NAME) || isRegistered) return;
        Messages.sendMessage("&cFailed to register flag and privilege for &b%s", SuperiorSkyblock.PLUGIN_NAME);
        Messages.sendMessage("&cIf you want to hook to &b%s&c don't use plugman to load this plugin!", SuperiorSkyblock.PLUGIN_NAME);
        Messages.sendMessage("&cYou have to restart the server to make sure &b%s&c plugin is hooked", SuperiorSkyblock.PLUGIN_NAME);
    }

    @EventHandler
    public void onInitialize(PluginInitializeEvent e) {
        IslandFlag.register(SuperiorSkyblock.FLAG_PRIVILEGE);
        Messages.sendMessage("&aRegistered flag &6%s", SuperiorSkyblock.FLAG_PRIVILEGE);
        IslandPrivilege.register(SuperiorSkyblock.FLAG_PRIVILEGE);
        Messages.sendMessage("&aRegistered privilege &6%s", SuperiorSkyblock.FLAG_PRIVILEGE);
        isRegistered = true;
    }

    @EventHandler
    public void onIslandChangeRolePrivilegeEvent(@NotNull IslandChangeRolePrivilegeEvent e) {
        onChangeFlagPrivilegeEvent(e.getIsland().getAllPlayersInside());
    }

    @EventHandler
    public void onIslandDisableFlagEvent(@NotNull IslandDisableFlagEvent e) {
        onChangeFlagPrivilegeEvent(e.getIsland().getAllPlayersInside());
    }

    private void onChangeFlagPrivilegeEvent(@NotNull List<SuperiorPlayer> players) {
        players.forEach(superiorPlayer -> {
            val player = superiorPlayer.asPlayer();
            if (player == null) return;
            val fj2RPlayer = FJ2RPlayer.getAsFJ2RPlayer(player);
            if (!fj2RPlayer.isActive()) return;

            if (fj2RPlayer.isAllowedSuperiorSkyblock()) return;
            fj2RPlayer.turnOff();
            Messages.sendMessage(player, Configs.getMessage().getSuperiorSkyblock2NoPermission());

        });
    }


}
