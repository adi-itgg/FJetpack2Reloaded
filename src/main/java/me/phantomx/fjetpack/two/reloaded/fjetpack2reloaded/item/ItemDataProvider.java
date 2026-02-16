package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.ItemDataKey;
import org.bukkit.inventory.ItemStack;

public interface ItemDataProvider {

    boolean isSupported();

    void setString(ItemStack item, ItemDataKey key, String value);

    String getString(ItemStack item, ItemDataKey key, String defaultValue);

    void setLong(ItemStack item, ItemDataKey key, Long value);

    Long getLong(ItemStack item, ItemDataKey key, Long defaultValue);

}
