package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.item;

import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ServerVersion;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ItemTags;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.StoredKey;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * Reads/writes the plugin tags of an {@link ItemStack} (persistent data container on 1.18+,
 * NMS reflection below that).
 * <p>
 * Owns the per-server "run id" that marks a jetpack as active for this plugin instance. The tag keys
 * stay private to this class: callers go through the operations of {@link ItemTags}.
 */
@Singleton
public class ItemTagCodec implements ItemTags {

    private static final int RUN_ID_DRAWS = 5;

    private final JavaPlugin plugin;
    private final ServerVersion version;
    private final Random random = new Random();
    private final int runId = nextRunId();

    public ItemTagCodec(@NotNull JavaPlugin plugin, @NotNull ServerVersion version) {
        this.plugin = plugin;
        this.version = version;
    }

    /** New per-activation id for the currently worn jetpack. */
    @Override
    public int newItemId() {
        return random.nextInt();
    }

    private int nextRunId() {
        for (int i = 0; i < RUN_ID_DRAWS; i++)
            random.nextInt();
        return random.nextInt();
    }

    /**
     * set jetpack id to {@link ItemStack}
     *
     * @param id set null to delete/remove
     */
    @Override
    public @NotNull ItemStack setJetpack(@NotNull ItemStack itemStack, @Nullable String id) {
        return putString(itemStack, StoredKey.PLUGIN_ID, id);
    }

    @Override
    public @NotNull String getJetpackID(@NotNull ItemStack itemStack, @NotNull String defaultValue) {
        return getStringOrDefault(itemStack, StoredKey.PLUGIN_ID, defaultValue);
    }

    @Override
    public @NotNull ItemStack setCustomFuelID(@NotNull ItemStack itemStack, @Nullable String id) {
        return putString(itemStack, StoredKey.CUSTOM_FUEL_ID, id);
    }

    @Override
    public @NotNull String getCustomFuelID(@NotNull ItemStack itemStack) {
        return getStringOrDefault(itemStack, StoredKey.CUSTOM_FUEL_ID, "UNKNOWN-404");
    }

    @Override
    public @NotNull ItemStack setFuelValue(@NotNull ItemStack itemStack, @Nullable Long value) {
        return putString(itemStack, StoredKey.FUEL_VALUE_ID, value != null ? String.valueOf(value) : null);
    }

    @Override
    public @Nullable Long getFuelValue(@NotNull ItemStack itemStack) {
        val result = getStringOrDefault(itemStack, StoredKey.FUEL_VALUE_ID, "Error");
        if (result.equals("Error")) return null;
        return Long.parseLong(result);
    }

    @Override
    public @NotNull ItemStack setActiveJetpack(@NotNull ItemStack item, int id) {
        item = putString(item, StoredKey.JETPACK_RUN_PLUGIN_ID, StoredKey.JETPACK_RUN_PLUGIN_ID + runId);
        item = putString(item, StoredKey.ACTIVE_JETPACK_ID, StoredKey.ACTIVE_JETPACK_ID + id);
        return item;
    }

    @Override
    public boolean isActiveJetpack(@NotNull ItemStack item, int id) {
        val isInstanceMatch = getStringOrDefault(item, StoredKey.JETPACK_RUN_PLUGIN_ID, "").equals(StoredKey.JETPACK_RUN_PLUGIN_ID + runId);
        return isInstanceMatch && getStringOrDefault(item, StoredKey.ACTIVE_JETPACK_ID, "").equals(StoredKey.ACTIVE_JETPACK_ID + id);
    }

    @Override
    public @NotNull ItemStack clearTag(@NotNull ItemStack itemStack, @NotNull String key) {
        return putString(itemStack, key, null);
    }

    @Override
    public boolean isNotItemArmor(@NotNull ItemStack item) {
        // Material names are stable across versions. The old NMS ItemArmor check is gone from
        // 1.20.5+ and its class name never resolves on a flattened CraftBukkit package.
        return !isItemArmorFast(item);
    }

    @Override
    public boolean isItemArmorFast(@NotNull ItemStack item) {
        final String typeNameString = item.getType().name();
        return typeNameString.endsWith("_HELMET")
                || typeNameString.endsWith("_CHESTPLATE")
                || typeNameString.endsWith("_LEGGINGS")
                || typeNameString.endsWith("_BOOTS");
    }

    /**
     * Legacy CraftBukkit bridge, only reached on servers up to 1.17. The version segment is part of
     * the package there; from 1.20.5 on the package is flattened, so an empty segment adds nothing.
     */
    private @NotNull Class<?> craftItemStack() throws ClassNotFoundException {
        val api = version.getNmsApiVersion();
        return Class.forName("org.bukkit.craftbukkit." + (api.isEmpty() ? "" : api + ".") + "inventory.CraftItemStack");
    }

    @SneakyThrows
    private @NotNull ItemStack putString(@NotNull ItemStack itemStack, @NotNull String key, @Nullable String value) {
        if (version.getServerVersion() > 17) {
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

        val craftItem = craftItemStack();
        val asNMSCopy = craftItem.getMethod("asNMSCopy", ItemStack.class);
        val nmsItemStack = asNMSCopy.invoke(craftItem, itemStack);
        var nbtTagCompound = nmsItemStack.getClass().getMethod("getTag").invoke(nmsItemStack);
        if (nbtTagCompound == null) {
            val nbtClass = Class.forName(version.getServerVersion() > 16 ? "net.minecraft.nbt.NBTTagCompound" :
                    String.format("net.minecraft.server.%s.NBTTagCompound", version.getNmsApiVersion())
            );
            nbtTagCompound = nbtClass.getDeclaredConstructor().newInstance();
        }
        // Resolve by exact signature: getMethod(name) looks for a no-argument method, which the NBT
        // compound has never had. The parameter types below are the same on every supported NMS.
        if (value == null)
            nbtTagCompound.getClass().getMethod("remove", String.class).invoke(nbtTagCompound, key);
        else
            nbtTagCompound.getClass().getMethod("setString", String.class, String.class).invoke(nbtTagCompound, key, value);
        val setTag = nmsItemStack.getClass().getMethod("setTag", nbtTagCompound.getClass());
        setTag.invoke(nmsItemStack, nbtTagCompound);
        val asBukkitCopy = craftItem.getMethod("asBukkitCopy", nmsItemStack.getClass());
        itemStack = (ItemStack) asBukkitCopy.invoke(craftItem, nmsItemStack);
        return itemStack;
    }

    @SneakyThrows
    private @NotNull String getStringOrDefault(@NotNull ItemStack itemStack, @NotNull String key, @NotNull String defaultValue) {
        if (version.getServerVersion() > 17) {
            val itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) return defaultValue;
            val namespacedKey = new NamespacedKey(plugin, key);
            val persistentData = itemMeta.getPersistentDataContainer();
            return persistentData.getOrDefault(namespacedKey, PersistentDataType.STRING, defaultValue);
        }
        val craftItem = craftItemStack();
        val asNMSCopy = craftItem.getMethod("asNMSCopy", ItemStack.class);
        val nmsItemStack = asNMSCopy.invoke(craftItem, itemStack);
        val nbt = nmsItemStack.getClass().getMethod("getTag").invoke(nmsItemStack);
        if (nbt == null) return defaultValue;
        val result = nbt.getClass().getMethod("getString", String.class).invoke(nbt, key);
        return result == null ? defaultValue : (String) result;
    }

}
