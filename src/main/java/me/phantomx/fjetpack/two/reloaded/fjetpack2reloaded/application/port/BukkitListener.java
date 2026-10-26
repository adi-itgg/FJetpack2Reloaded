package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

/**
 * A Bukkit event listener that knows how to register itself.
 * <p>
 * One event, one class: the listener is the adapter for exactly one Bukkit event type, so the plugin
 * entry point registers them all the same way and no class accumulates a growing pile of
 * {@code @EventHandler} methods. The useful logic stays where it was, in the {@code application}
 * layer - this only translates the event.
 * <p>
 * {@link #registerEvent} is deliberately defaulted: registration is one call for every listener, and
 * a test can hand in a plain {@link PluginManager}.
 */
public interface BukkitListener extends Listener {

    /**
     * @param manager the server's plugin manager
     * @param plugin  the plugin the listeners belong to; Bukkit also uses it to decide when they are
     *                dropped again
     */
    default void registerEvent(@NotNull PluginManager manager, @NotNull Plugin plugin) {
        manager.registerEvents(this, plugin);
    }

}
