package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item;

import lombok.SneakyThrows;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.StoredKey;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Version;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Reads/writes the plugin tags of an {@link ItemStack} (persistent data container on 1.18+,
 * NMS reflection below that).
 * <p>
 * Owns the per-server "run id" that marks a jetpack as active for this plugin instance.
 */
public class ItemTagCodec {

    private static final int RUN_ID_DRAWS = 5;

    private final Plugin plugin;
    private final Random random = new Random();
    private final int runId = nextRunId();

    public ItemTagCodec(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    /** New per-activation id for the currently worn jetpack. */
    public int newItemId() {
        return random.nextInt();
    }

    private int nextRunId() {
        for (int i = 0; i < RUN_ID_DRAWS; i++)
            random.nextInt();
        return random.nextInt();
    }

    public void setParticle(@NotNull ItemStack itemStack, String particleName) {
        putString(itemStack, StoredKey.PARTICLE_ID, particleName);
    }

    public @NotNull String getParticle(@NotNull ItemStack itemStack) {
        return getStringOrDefault(itemStack, StoredKey.PARTICLE_ID, "");
    }

    /**
     * set jetpack id to {@link ItemStack}
     *
     * @param id set null to delete/remove
     */
    public @NotNull ItemStack setJetpack(ItemStack itemStack, @Nullable String id) {
        return putString(itemStack, StoredKey.PLUGIN_ID, id);
    }

    public @NotNull ItemStack setCustomFuelID(ItemStack itemStack, @Nullable String id) {
        return putString(itemStack, StoredKey.CUSTOM_FUEL_ID, id);
    }

    public @NotNull String getCustomFuelID(ItemStack itemStack) {
        return getStringOrDefault(itemStack, StoredKey.CUSTOM_FUEL_ID, "UNKNOWN-404");
    }

    public @NotNull ItemStack setFuelValue(ItemStack itemStack, @Nullable Long value) {
        return putString(itemStack, StoredKey.FUEL_VALUE_ID, value != null ? String.valueOf(value) : null);
    }

    public @Nullable Long getFuelValue(ItemStack itemStack) {
        val result = getStringOrDefault(itemStack, StoredKey.FUEL_VALUE_ID, "Error");
        if (result.equals("Error")) return null;
        return Long.parseLong(result);
    }

    @SneakyThrows
    public @NotNull ItemStack putString(ItemStack itemStack, @NotNull String key, @Nullable String value) {
        if (Version.getServerVersion() > 17) {
            // Minimum server v1.14.x
            val itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) throw new NullPointerException("item meta is null!");
            val namespacedKey = new NamespacedKey(plugin, key);
            if (value == null)
                itemMeta.getPersistentDataContainer().remove(namespacedKey);
            else
                itemMeta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value);

            itemStack.setItemMeta(itemMeta);
            return itemStack;
        }

        val craftItem = Class.forName(String.format("org.bukkit.craftbukkit.%s.inventory.CraftItemStack", Version.getNmsApiVersion()));
        val asNMSCopy = craftItem.getMethod("asNMSCopy", ItemStack.class);
        val nmsItemStack = asNMSCopy.invoke(craftItem, itemStack);
        var nbtTagCompound = nmsItemStack.getClass().getMethod("getTag").invoke(nmsItemStack);
        if (nbtTagCompound == null) {
            nbtTagCompound = Class.forName(Version.getServerVersion() > 16 ? "net.minecraft.nbt.NBTTagCompound" :
                    String.format("net.minecraft.server.%s.NBTTagCompound", Version.getNmsApiVersion())
            );
            nbtTagCompound = nbtTagCompound.getClass().getDeclaredConstructors()[0].newInstance();
        }
        if (value == null)
            nbtTagCompound.getClass().getMethod("remove").invoke(nbtTagCompound, key);
        else
            nbtTagCompound.getClass().getMethod("setString").invoke(nbtTagCompound, key, value);
        val setTag = nmsItemStack.getClass().getMethod("setTag", nbtTagCompound.getClass());
        setTag.invoke(nmsItemStack, nbtTagCompound);
        val asBukkitCopy = craftItem.getMethod("asBukkitCopy", nmsItemStack.getClass());
        itemStack = (ItemStack) asBukkitCopy.invoke(craftItem, nmsItemStack);
        return itemStack;
    }

    public @NotNull String getJetpackID(@NotNull ItemStack itemStack, @NotNull String defaultValue) {
        return getStringOrDefault(itemStack, StoredKey.PLUGIN_ID, defaultValue);
    }

    @SneakyThrows
    private @NotNull String getStringOrDefault(ItemStack itemStack, @NotNull String key, @NotNull String defaultValue) {
        if (Version.getServerVersion() > 17) {
            val itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) return defaultValue;
            val namespacedKey = new NamespacedKey(plugin, key);
            val persistentData = itemMeta.getPersistentDataContainer();
            return persistentData.getOrDefault(namespacedKey, PersistentDataType.STRING, defaultValue);
        }
        val craftItem = Class.forName(String.format("org.bukkit.craftbukkit.%s.inventory.CraftItemStack", Version.getNmsApiVersion()));
        val asNMSCopy = craftItem.getMethod("asNMSCopy", ItemStack.class);
        val nmsItemStack = asNMSCopy.invoke(craftItem, itemStack);
        val nbt = nmsItemStack.getClass().getMethod("getTag").invoke(nmsItemStack);
        if (nbt == null) return defaultValue;
        val result = nbt.getClass().getMethod("getString", String.class).invoke(nbt, key);
        return result == null ? defaultValue : (String) result;
    }

    @SneakyThrows
    public static boolean isNotItemArmor(@NotNull ItemStack item) {
        val craftItem = Class.forName(String.format("org.bukkit.craftbukkit.%s.inventory.CraftItemStack", Version.getNmsApiVersion()));
        val asNMSCopy = craftItem.getMethod("asNMSCopy", ItemStack.class);
        val nmsItem = asNMSCopy.invoke(craftItem, item);
        val itm = nmsItem.getClass().getMethod(Version.getServerVersion() > 17 ? "c" : "getItem").invoke(nmsItem);
        var isNotArmor = !itm.getClass().getName().equals(
                Version.getServerVersion() > 16
                        ? "net.minecraft.world.item.ItemArmor"
                        : String.format("net.minecraft.server.%s.ItemArmor", Version.getNmsApiVersion())
        );
        if (isNotArmor)
            isNotArmor = switch (item.getType()) {
                case LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS -> false;
                default -> true;
            };
        return isNotArmor;
    }

    public static boolean isItemArmorFast(@NotNull ItemStack item) {
        final String typeNameString = item.getType().name();
        return typeNameString.endsWith("_HELMET")
                || typeNameString.endsWith("_CHESTPLATE")
                || typeNameString.endsWith("_LEGGINGS")
                || typeNameString.endsWith("_BOOTS");
    }

    public @NotNull ItemStack setActiveJetpack(@NotNull ItemStack item, int id) {
        item = putString(item, StoredKey.JETPACK_RUN_PLUGIN_ID, StoredKey.JETPACK_RUN_PLUGIN_ID + runId);
        item = putString(item, StoredKey.ACTIVE_JETPACK_ID, StoredKey.ACTIVE_JETPACK_ID + id);
        return item;
    }

    public boolean isActiveJetpack(@NotNull ItemStack item, int id) {
        val isInstanceMatch = getStringOrDefault(item, StoredKey.JETPACK_RUN_PLUGIN_ID, "").equals(StoredKey.JETPACK_RUN_PLUGIN_ID + runId);
        return isInstanceMatch && getStringOrDefault(item, StoredKey.ACTIVE_JETPACK_ID, "").equals(StoredKey.ACTIVE_JETPACK_ID + id);
    }

}
