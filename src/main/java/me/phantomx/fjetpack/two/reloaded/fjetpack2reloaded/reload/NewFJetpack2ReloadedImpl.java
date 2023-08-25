package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.reload;

import lombok.Getter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.FJ2RPlayerManager;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.Configs;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.event.RegisterEvent;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.hook.event.SuperiorSkyblockEvent;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.logging.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.model.NewFJ2RPlayer;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Version;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.UUID;
import java.util.WeakHashMap;

class NewFJetpack2ReloadedImpl extends JavaPlugin implements NewFJetpack2Reloaded {

    private final int metricsId = 17668;
    private final @NotNull Random random = new Random();
    private final int uniqueId = generateRandomId();
    private final @NotNull WeakHashMap<UUID, NewFJ2RPlayer> FJ2RPlayers = new WeakHashMap<>();
    private boolean isShutdown = false;



    private int generateRandomId() {
        for (int i = 0; i < 5; i++)
            random.nextInt();
        return random.nextInt();
    }
    private static @Getter NewFJetpack2ReloadedImpl instance;


    @Override
    public boolean isShutdown() {
        return isShutdown;
    }

    @Override
    public int getUniqueId() {
        return uniqueId;
    }

    @Override
    public @NotNull WeakHashMap<UUID, NewFJ2RPlayer> getFJ2RPlayers() {
        return FJ2RPlayers;
    }

    @Override
    public void onEnable() {
        instance = this;
        Log.log("is server support: %b - %s", Version.isServerSupport(), uniqueId);
        if (!Version.isServerSupport()) {
            setEnabled(false);
            return;
        }

        getServer().getPluginManager().registerEvents(new SuperiorSkyblockEvent(), this);
        getServer().getPluginManager().registerEvents(new RegisterEvent(), this);

        Configs.reloadConfig();

        new Metrics(this, metricsId);
    }

    @Override
    public void onDisable() {
        isShutdown = true;
        FJ2RPlayers.values().forEach(FJ2RPlayerManager::turnOff);
        FJ2RPlayers.clear();
        Configs.getJetpacksLoaded().clear();
    }

}
