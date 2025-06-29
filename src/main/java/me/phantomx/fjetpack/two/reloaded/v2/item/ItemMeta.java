package me.phantomx.fjetpack.two.reloaded.v2.item;

import lombok.SneakyThrows;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.StoredKey;
import me.phantomx.fjetpack.two.reloaded.v2.FJReloaded;
import me.phantomx.fjetpack.two.reloaded.v2.util.VersionManager;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static me.phantomx.fjetpack.two.reloaded.v2.util.Utils.toInt;

public class ItemMeta {

    private final FJReloaded plugin;
    private final VersionManager versionManager;

    private Class<?> classCraftItemStack;
    private Class<?> classNBTTagCompound;

    private Constructor<?> constructorNBTTagCompound;

    private Method methodAsNMSCopy;
    private Method methodSetStringNBTTag;
    private Method methodRemoveNBTTag;
    private Method methodGetStringNBTTag;

    // lazy init
    private Method lazyMethodGetTag;
    private Method lazyMethodSetTag;
    private Method lazyMethodAsBukkitCopy;
    private Method lazyMethodGetItemNMSItem;

    @SneakyThrows
    public ItemMeta(FJReloaded plugin) {
        this.plugin = plugin;
        this.versionManager = plugin.versionManager();

        // setup for older versions only
        if (versionManager.serverVersion() > 17) {
            return;
        }

        // must be cached for better performance
        this.classCraftItemStack = Class.forName("org.bukkit.craftbukkit." + versionManager.nmsApiVersion() + ".inventory.CraftItemStack");
        this.methodAsNMSCopy = classCraftItemStack.getMethod("asNMSCopy", ItemStack.class);
        final String nbtTagClassName;
        if (versionManager.serverVersion() > 16) {
            nbtTagClassName = "net.minecraft.nbt.NBTTagCompound";
        } else {
            nbtTagClassName = "net.minecraft.server." + versionManager.nmsApiVersion() + ".NBTTagCompound";
        }

        this.classNBTTagCompound = Class.forName(nbtTagClassName);
        this.constructorNBTTagCompound = classNBTTagCompound.getDeclaredConstructors()[0];
        this.methodSetStringNBTTag = classNBTTagCompound.getMethod("setString", String.class, String.class);
        this.methodRemoveNBTTag = classNBTTagCompound.getMethod("remove", String.class);
        this.methodGetStringNBTTag = classNBTTagCompound.getMethod("getString", String.class);
    }

    public ItemStack putInt(ItemStack itemStack, @NotNull String key, @Nullable Integer value) {
        return putString(itemStack, key, value == null ? null : value.toString());
    }

    @SneakyThrows
    public ItemStack putString(ItemStack itemStack, @NotNull String key, @Nullable String value) {
        // this new api is only available on 1.14+
        if (versionManager.serverVersion() > 17) {
            val itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                throw new NullPointerException("item meta is null!");
            }
            val namespacedKey = new NamespacedKey(plugin, key);
            if (value == null) {
                itemMeta.getPersistentDataContainer().remove(namespacedKey);
            } else {
                itemMeta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value);
            }

            itemStack.setItemMeta(itemMeta);
            return itemStack;
        }

        // using cached reflection for old versions
        val nmsItemStack = methodAsNMSCopy.invoke(classCraftItemStack, itemStack);

        if (lazyMethodGetTag == null) {
            lazyMethodGetTag = nmsItemStack.getClass().getMethod("getTag");
        }

        var nbtTagCompound = lazyMethodGetTag.invoke(nmsItemStack);
        if (nbtTagCompound == null) {
            nbtTagCompound = constructorNBTTagCompound.newInstance();
        }

        if (value == null) {
            methodRemoveNBTTag.invoke(nbtTagCompound, key);
        } else {
            methodSetStringNBTTag.invoke(nbtTagCompound, key, value);
        }

        if (lazyMethodSetTag == null) {
            lazyMethodSetTag = nmsItemStack.getClass().getMethod("setTag", classNBTTagCompound);
        }

        lazyMethodSetTag.invoke(nmsItemStack, nbtTagCompound);

        if (lazyMethodAsBukkitCopy == null) {
            lazyMethodAsBukkitCopy = classCraftItemStack.getMethod("asBukkitCopy", nmsItemStack.getClass());
        }

        itemStack = (ItemStack) lazyMethodAsBukkitCopy.invoke(classCraftItemStack, nmsItemStack);

        return itemStack;
    }

    public Integer getIntegerOrDefault(ItemStack itemStack, @NotNull String key, @NotNull Integer defaultValue) {
        return toInt(getStringOrDefault(itemStack, key, defaultValue.toString()), defaultValue);
    }


    @SneakyThrows
    public String getStringOrDefault(ItemStack itemStack, @NotNull String key, @NotNull String defaultValue) {
        // this new api is only available on 1.14+
        if (versionManager.serverVersion() > 17) {
            val itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                return defaultValue;
            }
            val namespacedKey = new NamespacedKey(plugin, key);
            val persistentData = itemMeta.getPersistentDataContainer();
            return persistentData.getOrDefault(namespacedKey, PersistentDataType.STRING, defaultValue);
        }


        val nmsItemStack = methodAsNMSCopy.invoke(classCraftItemStack, itemStack);

        val nbt = lazyMethodGetTag.invoke(nmsItemStack);
        if (nbt == null) {
            return defaultValue;
        }

        val result = (String) methodGetStringNBTTag.invoke(nbt, key);

        return result == null ? defaultValue : result;
    }

    @SneakyThrows
    public boolean isNotItemArmor(@NotNull ItemStack item) {
        val nmsItem = methodAsNMSCopy.invoke(classCraftItemStack, item);

        if (lazyMethodGetItemNMSItem == null) {
            final String methodName;
            if (versionManager.serverVersion() > 17) {
                methodName = "c";
            } else {
                methodName = "getItem";
            }
            lazyMethodGetItemNMSItem = nmsItem.getClass().getMethod(methodName);
        }


        val itm = lazyMethodGetItemNMSItem.invoke(nmsItem);
        final String className;
        if (versionManager.serverVersion() > 16) {
            className = "net.minecraft.world.item.ItemArmor";
        } else {
            className = "net.minecraft.server." + versionManager.nmsApiVersion() + ".ItemArmor";
        }

        var isNotArmor = !itm.getClass().getName().equals(className);
        if (isNotArmor) {
            isNotArmor = switch (item.getType()) {
                case LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS -> false;
                default -> true;
            };
        }

        return isNotArmor;
    }

    public boolean isItemArmorFast(@NotNull ItemStack item) {
        final String typeNameString = item.getType().name();
        return typeNameString.endsWith("_HELMET")
                || typeNameString.endsWith("_CHESTPLATE")
                || typeNameString.endsWith("_LEGGINGS")
                || typeNameString.endsWith("_BOOTS");
    }

    public @NotNull ItemStack setActiveJetpack(@NotNull ItemStack item, int id) {
        try {
            item = putInt(item, StoredKey.JETPACK_RUN_PLUGIN_ID, plugin.uniqueId());
            item = putInt(item, StoredKey.ACTIVE_JETPACK_ID, id);
        } catch (Throwable error) {
            plugin.getLogger().warning("Failed to set active jetpack: " + error.getMessage());
        }
        return item;
    }

    public boolean isActiveJetpack(@NotNull ItemStack item, int id) {
        try {
            val isInstanceMatch = getIntegerOrDefault(item, StoredKey.JETPACK_RUN_PLUGIN_ID, -1).equals(plugin.uniqueId());
            return isInstanceMatch && getIntegerOrDefault(item, StoredKey.ACTIVE_JETPACK_ID, -1).equals(id);
        } catch (Throwable error) {
            plugin.getLogger().warning("Failed to check active jetpack: " + error.getMessage());
        }
        return false;
    }


    public @NotNull ItemStack setJetpack(ItemStack itemStack, @Nullable String id) {
        return putString(itemStack, StoredKey.PLUGIN_ID, id);
    }

    public @NotNull String getJetpackID(@NotNull ItemStack itemStack, @NotNull String defaultValue) {
        return getStringOrDefault(itemStack, StoredKey.PLUGIN_ID, defaultValue);
    }

}
