package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.Configs;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Placeholder;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.IMessageException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Version;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;

public class ItemUtil {

    public static @NotNull Catcher<ItemStack> createCustomFuelItem(@NotNull CommandSender sender, @NotNull String customFuelId, int amount) {
        return Catcher.create(() -> {
            val customFuel = Configs.getCustomFuelLoaded().get(customFuelId);
            if (customFuel == null) {
                Messages.sendMessage(sender, "&cFailed to get custom fuel with id &6%s", customFuelId);
                IMessageException.send();
            }
            var item = new ItemStack(customFuel.getItem());
            if (customFuel.isGlowing())
                item = ItemMetaData.putString(item, "ench", null);

            val itemMeta = item.getItemMeta();
            assert itemMeta != null;
            itemMeta.setDisplayName(customFuel.getDisplayName());
            itemMeta.setLore(customFuel.getLore());
            if (customFuel.isGlowing()) {
                itemMeta.addEnchant(Enchantment.LUCK, 1, false);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(itemMeta);

            item = ItemMetaData.setCustomFuelID(item, customFuel.getId());
            item.setAmount(amount);

            return item;
        });
    }

    public static @NotNull Catcher<ItemStack> createJetpackItem(@NotNull CommandSender sender, @NotNull String jetpackId, long fuelValue) {
        return Catcher.create(() -> {
            val jetpack = Configs.getJetpacksLoaded().get(jetpackId);
            if (jetpack == null) {
                Messages.sendMessage(sender, "&cJetpack &l%s &cdidn't exist", jetpackId);
                IMessageException.send();
            }
            val item = new ItemStack(jetpack.getItem());
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

    public static @NotNull Catcher<ItemStack> setItemAsJetpack(@NotNull CommandSender sender, @NotNull ItemStack item, @NotNull String jetpackId, long fuelValue) {
        return Catcher.create(() -> {
            val jetpack = Configs.getJetpacksLoaded().get(jetpackId);
            if (jetpack == null) {
                Messages.sendMessage(sender, "&cJetpack &l%s &cdidn't exist", jetpackId);
                IMessageException.send();
            }
            return setItemAsJetpack(sender, item, jetpack, fuelValue).getOrThrow();
        });
    }
    public static @NotNull Catcher<ItemStack> setItemAsJetpack(@NotNull CommandSender sender, @NotNull ItemStack item, @NotNull Jetpack jetpack, long fuelValue) {
        return Catcher.create(() -> {

            val customFuel = jetpack.getFuel().getCustomFuel();
            var fuelDisplay = jetpack.getFuel().getItem().name().replace("_", " ");
            if (customFuel != null)
                fuelDisplay = customFuel.getCustomDisplay().isEmpty() ? customFuel.getDisplayName() : customFuel.getCustomDisplay();
            if (customFuel == null)
                fuelDisplay = StringUtils.capitalize(fuelDisplay.toLowerCase());
            val finalFuelDisplay = fuelDisplay;

            val itemMeta = item.getItemMeta();
            if (itemMeta == null) {
                Messages.sendMessage(sender, "&cInvalid item");
                IMessageException.send();
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
                        .onFailure(err -> Messages.sendMessage(sender, "&cInvalid flag %s", flag));

            if (Version.getServerVersion() > 16)
                itemMeta.setUnbreakable(jetpack.isUnbreakable());

            item.setItemMeta(itemMeta);
            var resultItem = ItemMetaData.setJetpack(item, jetpack.getId());
            resultItem = ItemMetaData.setFuelValue(item, fuelValue);
            val finalResultItem = resultItem;

            for (String enchant : jetpack.getEnchantments())
                Catcher.createVoid(() -> {
                    val enchantName = enchant.split(":")[0];
                    val enchantLvl = Integer.parseInt(enchant.split(":")[1]);
                    @SuppressWarnings("deprecation")
                    val enchantment = Version.getServerVersion() > 16 ?
                            Enchantment.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase())) :
                            Enchantment.getByName(enchantName.toUpperCase());
                    assert enchantment != null;
                    finalResultItem.addUnsafeEnchantment(enchantment, enchantLvl);
                });

            return finalResultItem;
        });
    }

}
