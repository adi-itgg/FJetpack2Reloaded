package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.CustomItems;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ItemTags;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.CustomFuel;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.IMessageException;
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
    }

    public @NotNull Catcher<ItemStack> createCustomFuelItem(@NotNull CommandSender sender, @NotNull String customFuelId, int amount) {
        return Catcher.create(() -> {
            val customFuel = configs.customFuels().get(customFuelId);
            if (customFuel == null) {
                messages.send(sender, "&cFailed to get custom fuel with id &6%s", customFuelId);
                IMessageException.throwNow();
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
                itemMeta.addEnchant(Enchantment.LUCK, 1, false);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(itemMeta);

            item = itemTags.setCustomFuelID(item, customFuel.getId());
            item.setAmount(amount);

            return item;
        });
    }

    public @NotNull Catcher<ItemStack> createJetpackItem(@NotNull CommandSender sender, @NotNull String jetpackId, long fuelValue) {
        return Catcher.create(() -> {
            val jetpack = configs.jetpacks().get(jetpackId);
            if (jetpack == null) {
                messages.send(sender, "&cJetpack &l%s &cdidn't exist", jetpackId);
                IMessageException.throwNow();
            }
            val item = baseJetpackItem(sender, jetpack);
            val itemMeta = item.getItemMeta();
            assert itemMeta != null;

            if (itemMeta instanceof LeatherArmorMeta && jetpack.getItemColor() != null) {
                val color = jetpack.getItemColor();
                ((LeatherArmorMeta) itemMeta).setColor(Color.fromRGB(color.getR(), color.getG(), color.getB()));
            }
            item.setItemMeta(itemMeta);

            return setItemAsJetpack(sender, item, jetpack.getId(), fuelValue).getOrThrow();
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
            val jetpack = configs.jetpacks().get(jetpackId);
            if (jetpack == null) {
                messages.send(sender, "&cJetpack &l%s &cdidn't exist", jetpackId);
                IMessageException.throwNow();
            }
            return setItemAsJetpack(sender, item, jetpack, fuelValue).getOrThrow();
        });
    }

    public @NotNull Catcher<ItemStack> setItemAsJetpack(@NotNull CommandSender sender, @NotNull ItemStack item, @NotNull Jetpack jetpack, long fuelValue) {
        return Catcher.create(() -> {
            val finalFuelDisplay = getDisplayFuel(jetpack);

            val itemMeta = item.getItemMeta();
            if (itemMeta == null) {
                messages.send(sender, "&cInvalid item");
                IMessageException.throwNow();
            }
            if (jetpack.getCustomModelData() != -1)
                itemMeta.setCustomModelData(jetpack.getCustomModelData());
            itemMeta.setDisplayName(jetpack.getDisplayName());
            itemMeta.setLore(jetpack.getLore().stream()
                    .map(v -> v.replace(Placeholder.FUEL, finalFuelDisplay)
                            .replace(Placeholder.FUEL_VALUE, String.valueOf(fuelValue))
                    ).toList());

            for (String flag : jetpack.getFlags())
                Catcher.createVoid(() -> itemMeta.addItemFlags(ItemFlag.valueOf(flag.toUpperCase().trim())))
                        .onFailure(err -> messages.send(sender, "&cInvalid flag %s", flag));

            if (version.getServerVersion() > 16)
                itemMeta.setUnbreakable(jetpack.isUnbreakable());

            item.setItemMeta(itemMeta);
            var resultItem = itemTags.setJetpack(item, jetpack.getId());
            resultItem = itemTags.setFuelValue(item, fuelValue);
            val finalResultItem = resultItem;

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
                });

            return finalResultItem;
        });
    }

    /** The jetpack configured for this item, or null when the item is not a jetpack. */
    public @Nullable Jetpack jetpackOf(@Nullable ItemStack item) {
        if (item == null) return null;
        return configs.jetpacks().get(itemTags.getJetpackID(item, ""));
    }

    /** Jetpack id stored on the item, or "" when there is none. */
    public @NotNull String jetpackIdOf(@NotNull ItemStack item) {
        return itemTags.getJetpackID(item, "");
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
        if (meta == null || item.getType() == Material.AIR) {
            messages.send(player, "&cYou not holding any item in hand.");
            return null;
        }
        if (itemTags.isNotItemArmor(item)) {
            messages.send(player, "&cThis item is not armor item!");
            return null;
        }
        return item;
    }

}
