package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.CommandTabCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.FJ2RCommandExecutor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.Configs;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.FJ2RPlayer;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.event.RegisterEvent;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.hook.event.SuperiorSkyblockEvent;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.logging.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Version;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.WeakHashMap;

public class FJetpack2Reloaded extends JavaPlugin {

    private static final int metricsId = 17668;

    @Getter @Setter(AccessLevel.PRIVATE)
    private static @NotNull FJetpack2Reloaded plugin;
    @Getter @Setter(AccessLevel.PRIVATE)
    private static int uniqueId;

    @Getter
    private static final @NotNull WeakHashMap<UUID, FJ2RPlayer> FJ2RPlayers = new WeakHashMap<>();
    @Getter
    private static final @NotNull Random random = new Random();

    private void generateRandomId() {
        for (int i = 0; i < 5; i++)
            getRandom().nextInt();
        setUniqueId(getRandom().nextInt());
    }

    @Override
    public void onEnable() {
        setPlugin(this);
        generateRandomId();
        Log.log("is server support: %b - %s", Version.isServerSupport(), uniqueId);
        if (!Version.isServerSupport()) {
            setEnabled(false);
            return;
        }

        getServer().getPluginManager().registerEvents(new SuperiorSkyblockEvent(), plugin);
        getServer().getPluginManager().registerEvents(new RegisterEvent(), plugin);

        Configs.reloadConfig();

        new Metrics(this, metricsId);
    }

    @Override
    public void onDisable() {
        getFJ2RPlayers().values().forEach(activePlayer -> {
            activePlayer.turnOff(false, false, false, true);
        });
        getFJ2RPlayers().clear();
        Configs.getJetpacksLoaded().clear();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return Catcher.create(() -> FJ2RCommandExecutor.onCommand(sender, command, label, args))
                .onFailure(error -> {
                    if (error instanceof NoPermissionLvlException) {
                        Messages.sendMessage(sender, Configs.getMessage().getNoPermission());
                        return;
                    }
                    if (error instanceof NumberFormatException) {
                        Messages.sendMessage(sender, Configs.getMessage().getInvalidNumber());
                        return;
                    }
                    Messages.sendMessage("Command error!: %s", error);
                })
                .getOrDefault(false);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return CommandTabCompleter.onTab(sender, command, alias, args);
    }
}
