package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Lifecycle of the SuperiorSkyblock2 hook: announce it, then check a few seconds later whether the
 * flag and privilege actually got registered.
 * <p>
 * No event handler of its own - each SuperiorSkyblock event has its own listener class.
 */
@Singleton
public class SuperiorSkyblockHook {

    private static final long CHECK_DELAY_TICKS = 3 * 20L;

    private final JavaPlugin plugin;
    private final MessageSink messages;
    private final SuperiorSkyblockHookState state;

    public SuperiorSkyblockHook(@NotNull JavaPlugin plugin,
                                @NotNull MessageSink messages,
                                @NotNull SuperiorSkyblockHookState state) {
        this.plugin = plugin;
        this.messages = messages;
        this.state = state;
    }

    /**
     * Called once the plugin has decided to stay enabled. The bean constructor stays free of side
     * effects on purpose: building the object graph must not talk to the server.
     */
    public void hook() {
        if (isPluginEnabled())
            messages.sendToConsole("&aRegistering %s...", SuperiorSkyblock.PLUGIN_NAME);
        Bukkit.getScheduler().runTaskLater(plugin, this::checkIsHooked, CHECK_DELAY_TICKS);
    }

    private void checkIsHooked() {
        if (!isPluginEnabled() || state.isRegistered()) return;
        messages.sendToConsole("&cFailed to register flag and privilege for &b%s", SuperiorSkyblock.PLUGIN_NAME);
        messages.sendToConsole("&cIf you want to hook to &b%s&c don't use plugman to load this plugin!", SuperiorSkyblock.PLUGIN_NAME);
        messages.sendToConsole("&cYou have to restart the server to make sure &b%s&c plugin is hooked", SuperiorSkyblock.PLUGIN_NAME);
    }

    private static boolean isPluginEnabled() {
        val manager = Bukkit.getServer().getPluginManager();
        return manager != null && manager.isPluginEnabled(SuperiorSkyblock.PLUGIN_NAME);
    }

}
