package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook;

import com.bgsoftware.superiorskyblock.api.events.IslandChangeRolePrivilegeEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandDisableFlagEvent;
import com.bgsoftware.superiorskyblock.api.events.PluginInitializeEvent;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.PlayerRegistry;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook.SuperiorSkyblock;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.message.Messages;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SuperiorSkyblockEvent implements Listener {

    private final JavaPlugin plugin;
    private final YamlConfigRepository configs;
    private final Messages messages;
    private final PlayerRegistry players;

    private boolean isRegistered = false;

    public SuperiorSkyblockEvent(@NotNull JavaPlugin plugin,
                                 @NotNull YamlConfigRepository configs,
                                 @NotNull Messages messages,
                                 @NotNull PlayerRegistry players) {
        this.plugin = plugin;
        this.configs = configs;
        this.messages = messages;
        this.players = players;

        if (Bukkit.getServer().getPluginManager().isPluginEnabled(SuperiorSkyblock.PLUGIN_NAME))
            messages.sendMessage("&aRegistering %s...", SuperiorSkyblock.PLUGIN_NAME);
        Bukkit.getScheduler().runTaskLater(plugin, this::checkIsHooked, 3 * 20L);
    }

    private void checkIsHooked() {
        if (!Bukkit.getServer().getPluginManager().isPluginEnabled(SuperiorSkyblock.PLUGIN_NAME) || isRegistered) return;
        messages.sendMessage("&cFailed to register flag and privilege for &b%s", SuperiorSkyblock.PLUGIN_NAME);
        messages.sendMessage("&cIf you want to hook to &b%s&c don't use plugman to load this plugin!", SuperiorSkyblock.PLUGIN_NAME);
        messages.sendMessage("&cYou have to restart the server to make sure &b%s&c plugin is hooked", SuperiorSkyblock.PLUGIN_NAME);
    }

    @EventHandler
    public void onInitialize(PluginInitializeEvent e) {
        IslandFlag.register(SuperiorSkyblock.FLAG_PRIVILEGE);
        messages.sendMessage("&aRegistered flag &6%s", SuperiorSkyblock.FLAG_PRIVILEGE);
        IslandPrivilege.register(SuperiorSkyblock.FLAG_PRIVILEGE);
        messages.sendMessage("&aRegistered privilege &6%s", SuperiorSkyblock.FLAG_PRIVILEGE);
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
            val fj2RPlayer = this.players.of(player);
            if (!fj2RPlayer.isActive()) return;

            if (fj2RPlayer.isAllowedSuperiorSkyblock()) return;
            fj2RPlayer.turnOff();
            messages.sendMessage(player, configs.message().getSuperiorSkyblock2NoPermission());

        });
    }

}
