package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.ItemDataKey;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class PDCProvider implements ItemDataProvider {

    private final Plugin plugin;

    @Override
    public boolean isSupported() {
        return Try.of(() -> Class.forName("org.bukkit.persistence.PersistentDataContainer")).isSuccess();
    }

    @Override
    public void setString(ItemStack item, ItemDataKey key, String value) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        NamespacedKey nsk = new NamespacedKey(plugin, key.key());
        meta.getPersistentDataContainer().set(nsk, PersistentDataType.STRING, value);
        item.setItemMeta(meta);
    }

    @Override
    public String getString(ItemStack item, ItemDataKey key, String defaultValue) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return defaultValue;
        }
        NamespacedKey nsk = new NamespacedKey(plugin, key.key());
        return meta.getPersistentDataContainer().getOrDefault(nsk, PersistentDataType.STRING, defaultValue);
    }

    @Override
    public void setLong(ItemStack item, ItemDataKey key, Long value) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        NamespacedKey nsk = new NamespacedKey(plugin, key.key());
        meta.getPersistentDataContainer().set(nsk, PersistentDataType.LONG, value);
        item.setItemMeta(meta);
    }

    @Override
    public Long getLong(ItemStack item, ItemDataKey key, Long defaultValue) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return defaultValue;
        }
        NamespacedKey nsk = new NamespacedKey(plugin, key.key());
        return meta.getPersistentDataContainer().getOrDefault(nsk, PersistentDataType.LONG, defaultValue);
    }

}
