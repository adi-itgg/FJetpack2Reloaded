package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item;

import io.avaje.inject.Component;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.FJConfig;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.ItemDataKey;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.InfoLevelException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Placeholder;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc.FJVersion;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

@Component
@RequiredArgsConstructor
public class JetpackItemFactory {

    private final FJConfig config;
    private final FJVersion version;
    private final ItemDataProvider itemDataProvider;

    public ItemStack createCustomFuelItem(CommandSender sender, String customFuelId, int amount) {
        if (config.customFuels().isEmpty()) {
            throw new InfoLevelException("&cNo custom fuels loaded");
        }

        val customFuel = config.customFuels().get(customFuelId);
        if (customFuel == null) {
            throw new InfoLevelException("&cInvalid custom fuel id &6" + customFuelId);
        }

        val item = new ItemStack(customFuel.getItem());
        val itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            throw new IllegalStateException("Invalid item meta");
        }

        itemMeta.setDisplayName(customFuel.getDisplayName());
        itemMeta.setLore(customFuel.getLore());

        if (customFuel.isGlowing()) {
            itemMeta.addEnchant(Enchantment.LUCK, 1, false);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        itemMeta.setCustomModelData(customFuel.getCustomModelData());
        item.setItemMeta(itemMeta);

        itemDataProvider.setString(item, ItemDataKey.CUSTOM_FUEL_ID, customFuel.getId());
        item.setAmount(amount);

        return item;
    }

    @SuppressWarnings("deprecation")
    public ItemStack createJetpackItem(CommandSender sender, String jetpackId, long fuelValue) {
        val jetpack = config.jetpacks().get(jetpackId);
        if (jetpack == null) {
            throw new InfoLevelException("&cJetpack &l" + jetpackId + " &cdidn't exist");
        }

        val item = new ItemStack(jetpack.getItem());
        val itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            throw new IllegalStateException("Invalid item meta");
        }

        var lore = jetpack.getLore().stream().map(v -> {
            return v.replace(Placeholder.FUEL, getDisplayFuel(jetpack)
                    .replace(Placeholder.FUEL_VALUE, String.valueOf(fuelValue)));
        }).toList();

        itemMeta.setDisplayName(jetpack.getDisplayName());
        itemMeta.setLore(lore);

        if (itemMeta instanceof LeatherArmorMeta && jetpack.getItemColor() != null) {
            val color = jetpack.getItemColor();
            ((LeatherArmorMeta) itemMeta).setColor(Color.fromRGB(color.getR(), color.getG(), color.getB()));
        }

        for (String flag : jetpack.getFlags()) {
            Try.run(() -> itemMeta.addItemFlags(ItemFlag.valueOf(flag.toUpperCase().trim())))
                    .onFailure(err -> Messages.sendMessage(sender, "&cInvalid flag %s", flag));
        }

        if (version.getServerVersion() > 16) {
            itemMeta.setUnbreakable(jetpack.isUnbreakable());
        }

        itemMeta.setCustomModelData(jetpack.getCustomModelData());

        for (String enchantment : jetpack.getEnchantments()) {
            Try.run(() -> {
                val sp = enchantment.split(":");
                val enchantName = sp[0];
                val enchantLvl = Integer.parseInt(sp[1]);
                val enchantmentObj = version.getServerVersion() > 16 ?
                        Enchantment.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase())) :
                        Enchantment.getByName(enchantName.toUpperCase());
                if (enchantmentObj != null) {
                    itemMeta.addEnchant(enchantmentObj, enchantLvl, true);
                }
            }).onFailure(err -> Messages.sendMessage(sender, "&cInvalid enchantment %s", enchantment));
        }

        item.setItemMeta(itemMeta);
        return item;
    }

    private String getDisplayFuel(Jetpack jetpack) {
        val customFuel = jetpack.getFuel().getCustomFuel();
        var fuelDisplay = jetpack.getFuel().getItem().name().replace("_", " ");
        if (customFuel != null) {
            fuelDisplay = customFuel.getCustomDisplay().isEmpty() ? customFuel.getDisplayName() : customFuel.getCustomDisplay();
        }
        return (customFuel == null) ? StringUtils.capitalize(fuelDisplay.toLowerCase()) : fuelDisplay;
    }
}