package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Items owned by another plugin, looked up by the id that plugin uses.
 * <p>
 * The use cases never touch the other plugin's API - an adapter in {@code infrastructure} does, and
 * is the bean published for this port.
 */
public interface CustomItems {

    /** True when the plugin that provides these items is installed and enabled on this server. */
    boolean isAvailable();

    /**
     * A fresh stack of the custom item.
     *
     * @return null when the source is unavailable, the id is unknown, or its data has not been
     *         loaded yet - the caller decides what to fall back to
     */
    @Nullable
    ItemStack get(@NotNull String id);

}
