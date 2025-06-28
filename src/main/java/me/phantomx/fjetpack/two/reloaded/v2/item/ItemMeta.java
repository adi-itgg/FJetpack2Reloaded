package me.phantomx.fjetpack.two.reloaded.v2.item;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Version;
import me.phantomx.fjetpack.two.reloaded.v2.util.VersionManager;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ItemMeta {

    private final Plugin plugin;
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

    public ItemMeta(Plugin plugin, VersionManager versionManager) throws Throwable {
        this.plugin = plugin;
        this.versionManager = versionManager;

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


    public ItemStack putString(ItemStack itemStack, @NotNull String key, @Nullable String value) throws Throwable {
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


    public String getStringOrDefault(ItemStack itemStack, @NotNull String key, @NotNull String defaultValue) throws Throwable {
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

    // TODO last rewrite here...
    public boolean isNotItemArmor(@NotNull ItemStack item) throws Throwable {
        val nmsItem = methodAsNMSCopy.invoke(classCraftItemStack, item);
        val itm = nmsItem.getClass().getMethod(versionManager.serverVersion() > 17 ? "c" : "getItem").invoke(nmsItem); // TODO check why method is different like obfuscated
        var isNotArmor = !itm.getClass().getName().equals(
                versionManager.serverVersion() > 16
                        ? "net.minecraft.world.item.ItemArmor"
                        : String.format("net.minecraft.server.%s.ItemArmor", versionManager.nmsApiVersion())
        );
        if (isNotArmor)
            isNotArmor = switch (item.getType()) {
                case LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS -> false;
                default -> true;
            };
        return isNotArmor;
    }

}
