package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.CustomItems;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ItemTags;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.IMessageException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.Placeholder;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Builds and mutates the jetpack / custom fuel items from the loaded config.
 */
@Singleton
public class ItemFactory {

    private final ConfigRepository configs;
    private final ItemTags itemTags;
    private final MessageSink messages;
    private final ServerVersion version;
    private final CustomItems customItems;
    private final Log log;
    private final Enchantment GLOW_ENCHANT = resolveEnchant();

    public ItemFactory(@NotNull ConfigRepository configs,
                       @NotNull ItemTags itemTags,
                       @NotNull MessageSink messages,
                       @NotNull ServerVersion version,
                       @NotNull CustomItems customItems) {
        this.configs = configs;
        this.itemTags = itemTags;
        this.messages = messages;
        this.version = version;
        this.customItems = customItems;
        this.log = new Log(getClass(), messages);
    }

    public @NotNull Catcher<ItemStack> createCustomFuelItem(@NotNull CommandSender sender, @NotNull String customFuelId, int amount) {
        return Catcher.create(() -> {
            val customFuel = configs.customFuels().get(customFuelId);
            if (customFuel == null) {
                messages.send(sender, "&cFailed to get custom fuel with id &6%s", customFuelId);
                IMessageException.throwNow("custom fuel id '" + customFuelId + "' is not in custom_fuel.yml");
            }
            var item = new ItemStack(customFuel.getItem());
            if (customFuel.isGlowing())
                item = itemTags.clearTag(item, "ench");

            val itemMeta = item.getItemMeta();
            assert itemMeta != null;

            // set custom model data
            if (customFuel.getCustomModelData() != -1) {
                itemMeta.setCustomModelData(customFuel.getCustomModelData());
            }

            itemMeta.setDisplayName(customFuel.getDisplayName());
            itemMeta.setLore(customFuel.getLore());
            if (customFuel.isGlowing()) {
                item = addGlow(item);
            }
            item.setItemMeta(itemMeta);

            item = itemTags.setCustomFuelID(item, customFuel.getId());
            item.setAmount(amount);

            return item;
        });
    }

    public ItemStack addGlow(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || GLOW_ENCHANT == null) return item;

        meta.addEnchant(GLOW_ENCHANT, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    @SuppressWarnings("deprecation")
    private Enchantment resolveEnchant() {
        // 1.8 - 1.20.4 memakai "DURABILITY", 1.20.5+ memakai "UNBREAKING"
        Enchantment e = Enchantment.getByName("DURABILITY");
        if (e == null) e = Enchantment.getByName("UNBREAKING");
        return e;
    }

    public @NotNull Catcher<ItemStack> createJetpackItem(@NotNull CommandSender sender, @NotNull String jetpackId, long fuelValue) {
        return Catcher.create(() -> {
            log.debug("createJetpackItem: sender=%s jetpackId=%s fuel=%d serverVersion=%d",
                    sender.getName(), jetpackId, fuelValue, version.getServerVersion());
            val jetpack = configs.jetpacks().get(jetpackId);
            if (jetpack == null) {
                log.debug("createJetpackItem: unknown jetpackId=%s", jetpackId);
                messages.send(sender, "&cJetpack &l%s &cdidn't exist", jetpackId);
                IMessageException.throwNow("jetpack id '" + jetpackId + "' is not in jetpack.yml");
            }
            log.debug("createJetpackItem: config id=%s material=%s itemsAdder=%s",
                    jetpack.getId(), jetpack.getItem(), jetpack.usesItemsAdder());
            val item = baseJetpackItem(sender, jetpack);
            log.debug("createJetpackItem: base item=%s", Log.item(item));
            val itemMeta = item.getItemMeta();
            assert itemMeta != null;

            if (itemMeta instanceof LeatherArmorMeta && jetpack.getItemColor() != null) {
                val color = jetpack.getItemColor();
                ((LeatherArmorMeta) itemMeta).setColor(Color.fromRGB(color.getR(), color.getG(), color.getB()));
            }
            item.setItemMeta(itemMeta);

            val result = setItemAsJetpack(sender, item, jetpack.getId(), fuelValue).getOrThrow();
            if (log.isDebugEnabled())
                log.debug("createJetpackItem done: jetpackId=%s item=%s tags=[%s]", jetpackId, Log.item(result), describeTags(result));
            return result;
        });
    }

    /**
     * What the jetpack item is built on: the ItemsAdder item when one is configured, otherwise the
     * configured {@code Item} material.
     * <p>
     * ItemsAdder is optional, so a missing plugin, an unknown id or one whose data is not loaded yet
     * reports and falls back to the material. A jetpack is never withheld because of it.
     */
    private @NotNull ItemStack baseJetpackItem(@NotNull CommandSender sender, @NotNull Jetpack jetpack) {
        if (!jetpack.usesItemsAdder()) return new ItemStack(jetpack.getItem());

        if (jetpack.getItemsAdderID() == null) {
            messages.send(sender, "&eItemsAdder item id is NULL");
        }

        val itemsAdderId = jetpack.getItemsAdderID().trim();
        val itemsAdderItem = customItems.get(itemsAdderId);
        if (itemsAdderItem != null) return itemsAdderItem.clone();

        if (customItems.isAvailable())
            messages.send(sender, "&eItemsAdder item &6%s&e does not exist or is not loaded yet; using &6%s&e for jetpack &6%s",
                    itemsAdderId, jetpack.getItem(), jetpack.getId());
        else
            messages.send(sender, "&eItemsAdder is not installed; using &6%s&e for jetpack &6%s",
                    jetpack.getItem(), jetpack.getId());
        return new ItemStack(jetpack.getItem());
    }

    public @NotNull Catcher<ItemStack> setItemAsJetpack(@NotNull CommandSender sender, @NotNull ItemStack item, @NotNull String jetpackId, long fuelValue) {
        return Catcher.create(() -> {
            log.debug("setItemAsJetpack(id): sender=%s jetpackId=%s item=%s fuel=%d",
                    sender.getName(), jetpackId, Log.item(item), fuelValue);
            val jetpack = configs.jetpacks().get(jetpackId);
            if (jetpack == null) {
                log.debug("setItemAsJetpack(id): unknown jetpackId=%s", jetpackId);
                messages.send(sender, "&cJetpack &l%s &cdidn't exist", jetpackId);
                IMessageException.throwNow("jetpack id '" + jetpackId + "' is not in jetpack.yml");
            }
            return setItemAsJetpack(sender, item, jetpack, fuelValue).getOrThrow();
        });
    }

    public @NotNull Catcher<ItemStack> setItemAsJetpack(@NotNull CommandSender sender, @NotNull ItemStack item, @NotNull Jetpack jetpack, long fuelValue) {
        return Catcher.create(() -> {
            val finalFuelDisplay = getDisplayFuel(jetpack);
            log.debug("setItemAsJetpack: sender=%s jetpackId=%s item=%s fuel=%d displayFuel=%s serverVersion=%d",
                    sender.getName(), jetpack.getId(), Log.item(item), fuelValue, finalFuelDisplay, version.getServerVersion());

            val itemMeta = item.getItemMeta();
            if (itemMeta == null) {
                log.debug("setItemAsJetpack: item=%s has no item meta", Log.item(item));
                messages.send(sender, "&cInvalid item");
                IMessageException.throwNow("item " + Log.item(item) + " has no item meta");
            }
            if (jetpack.getCustomModelData() != -1)
                itemMeta.setCustomModelData(jetpack.getCustomModelData());
            itemMeta.setDisplayName(jetpack.getDisplayName());
            itemMeta.setLore(jetpack.getLore().stream()
                    .map(v -> v.replace(Placeholder.FUEL, finalFuelDisplay)
                            .replace(Placeholder.FUEL_VALUE, String.valueOf(fuelValue))
                    ).toList());
            log.debug("setItemAsJetpack: displayName=%s loreLines=%d customModelData=%s unbreakable=%s",
                    jetpack.getDisplayName(), jetpack.getLore().size(), jetpack.getCustomModelData(), jetpack.isUnbreakable());

            for (String flag : jetpack.getFlags())
                Catcher.createVoid(() -> itemMeta.addItemFlags(ItemFlag.valueOf(flag.toUpperCase().trim())))
                        .onFailure(err -> {
                            log.debug("setItemAsJetpack: invalid flag=%s: %s", flag, err);
                            messages.send(sender, "&cInvalid flag %s", flag);
                        });

            if (version.getServerVersion() > 16)
                itemMeta.setUnbreakable(jetpack.isUnbreakable());

            item.setItemMeta(itemMeta);
            var resultItem = itemTags.setJetpack(item, jetpack.getId());
            // Legacy NMS writes return a new ItemStack; reusing 'item' here drops the id tag.
            resultItem = itemTags.setFuelValue(resultItem, fuelValue);
            val finalResultItem = resultItem;
            if (log.isDebugEnabled())
                log.debug("setItemAsJetpack: after tags item=%s tags=[%s]", Log.item(finalResultItem), describeTags(finalResultItem));

            for (String enchant : jetpack.getEnchantments())
                Catcher.createVoid(() -> {
                    val enchantName = enchant.split(":")[0];
                    val enchantLvl = Integer.parseInt(enchant.split(":")[1]);
                    @SuppressWarnings("deprecation")
                    val enchantment = version.getServerVersion() > 16 ?
                            Enchantment.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase())) :
                            Enchantment.getByName(enchantName.toUpperCase());
                    assert enchantment != null;
                    finalResultItem.addUnsafeEnchantment(enchantment, enchantLvl);
                }).onFailure(err -> log.debug("setItemAsJetpack: enchant=%s failed: %s", enchant, err));

            if (log.isDebugEnabled())
                log.debug("setItemAsJetpack done: jetpackId=%s item=%s tags=[%s]", jetpack.getId(), Log.item(finalResultItem), describeTags(finalResultItem));
            return finalResultItem;
        });
    }

    /** The jetpack configured for this item, or null when the item is not a jetpack. */
    public @Nullable Jetpack jetpackOf(@Nullable ItemStack item) {
        if (item == null) {
            log.debug("jetpackOf: item is null");
            return null;
        }
        val id = itemTags.getJetpackID(item, "");
        val jetpack = configs.jetpacks().get(id);
        log.debug("jetpackOf: item=%s id=%s config=%s", Log.item(item), id, jetpack == null ? "null" : jetpack.getId());
        return jetpack;
    }

    /** Jetpack id stored on the item, or "" when there is none. */
    public @NotNull String jetpackIdOf(@NotNull ItemStack item) {
        val id = itemTags.getJetpackID(item, "");
        log.debug("jetpackIdOf: item=%s id=%s", Log.item(item), id);
        return id;
    }

    /** Read-back of the plugin tags, for debug logs only; never throws. */
    private @NotNull String describeTags(@NotNull ItemStack item) {
        try {
            return "id=" + itemTags.getJetpackID(item, "<none>") + ", fuel=" + itemTags.getFuelValue(item);
        } catch (Throwable error) {
            log.debug("describeTags FAILED: item=%s: %s", Log.item(item), error);
            return "<failed>";
        }
    }

    /** Pure formatting helper: how the fuel source is displayed in an item lore. */
    public static @NotNull String getDisplayFuel(@NotNull Jetpack jetpack) {
        val customFuel = jetpack.getFuel().getCustomFuel();
        var fuelDisplay = jetpack.getFuel().getItem().name().replace("_", " ");
        if (customFuel != null)
            fuelDisplay = customFuel.getCustomDisplay().isEmpty() ? customFuel.getDisplayName() : customFuel.getCustomDisplay();
        if (customFuel == null)
            fuelDisplay = StringUtils.capitalize(fuelDisplay.toLowerCase());
        return fuelDisplay;
    }

    /** Display the fuel of a worn jetpack item. */
    public void updateDisplayItem(@NotNull Jetpack jetpack, @NotNull ItemStack item, long finalFuel) {
        val itemMeta = item.getItemMeta();
        log.debug("updateDisplayItem: jetpackId=%s item=%s fuel=%d meta=%s",
                jetpack.getId(), Log.item(item), finalFuel, itemMeta == null ? "null" : "present");
        if (itemMeta != null) {
            val displayFuel = getDisplayFuel(jetpack);

            if (version.getServerVersion() > 16)
                itemMeta.setUnbreakable(jetpack.isUnbreakable());
            itemMeta.setDisplayName(jetpack.getDisplayName());
            itemMeta.setLore(jetpack.getLore().stream()
                    .map(v -> v.replace(Placeholder.FUEL, displayFuel)
                            .replace(Placeholder.FUEL_VALUE, String.valueOf(finalFuel))
                    ).toList());
            item.setItemMeta(itemMeta);
        }
    }

    /** Item in the main hand, validated as armor; messages the player when it is not usable. */
    public @Nullable ItemStack itemInHand(@NotNull Player player) {
        @SuppressWarnings("deprecation")
        val item = version.getServerVersion() > 8 ? player.getInventory().getItemInMainHand() : player.getItemInHand();
        val meta = item.getItemMeta();
        log.debug("itemInHand: player=%s item=%s material=%s meta=%s serverVersion=%d api=%s",
                player.getName(), Log.item(item), item.getType(), meta == null ? "null" : "present",
                version.getServerVersion(), version.getNmsApiVersion());
        if (meta == null || item.getType() == Material.AIR) {
            log.debug("itemInHand: player=%s -> no item in main hand", player.getName());
            messages.send(player, "&cYou not holding any item in hand.");
            return null;
        }
        if (itemTags.isNotItemArmor(item)) {
            log.debug("itemInHand: player=%s item=%s -> not armor", player.getName(), item.getType());
            messages.send(player, "&cThis item is not armor item!");
            return null;
        }
        log.debug("itemInHand: player=%s item=%s -> accepted", player.getName(), item.getType());
        return item;
    }

}
