package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook;

import dev.lone.itemsadder.api.CustomStack;
import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.CustomItems;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ItemsAdder custom items, behind {@link CustomItems}.
 * <p>
 * This is the only class that touches {@code dev.lone.itemsadder.*}. Its signatures stay clear of the
 * API on purpose: with ItemsAdder absent the bean still builds, and every lookup returns before the
 * API class is resolved - so no {@link NoClassDefFoundError} and no hard dependency.
 * <p>
 * ItemsAdder loads its items asynchronously, so no lookup happens while the config is read. A lookup
 * that runs too early just finds nothing and the caller falls back to the configured material; the
 * next lookup after {@code ItemsAdderLoadDataEvent} finds the item.
 */
@Singleton
public class ItemsAdderItems implements CustomItems {

    public static final String PLUGIN_NAME = "ItemsAdder";

    @Override
    public boolean isAvailable() {
        val manager = Bukkit.getPluginManager();
        return manager != null && manager.isPluginEnabled(PLUGIN_NAME);
    }

    @Override
    public @Nullable ItemStack get(@NotNull String id) {
        if (!isAvailable()) return null;
        try {
            val stack = CustomStack.getInstance(id);
            return stack == null ? null : stack.getItemStack();
        } catch (RuntimeException | LinkageError error) {
            // /iareload can leave the API half-initialised for a moment; a null falls back.
            return null;
        }
    }

}
