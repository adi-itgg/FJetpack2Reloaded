package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.FJConfig;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.ItemDataKey;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.InfoLevelException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemDataProvider;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Placeholder;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public abstract class CommandExtensionPlugin {

    protected final Server server;
    protected final FJConfig config;
    protected final JavaPlugin plugin;
    protected final Logger log;
    protected final FJVersion version;
    protected final ItemDataProvider itemDataProvider;

    protected List<String> copyPartialMatches(String token, Iterable<String> suggest) {
        return StringUtil.copyPartialMatches(token, suggest, new ArrayList<>());
    }

    protected List<String> getOnlinePlayers() {
        val onlinePlayers = new ArrayList<String>();
        for (Player player : server.getOnlinePlayers()) {
            onlinePlayers.add(player.getName());
            onlinePlayers.add(player.getDisplayName());
        }
        return onlinePlayers.stream().distinct().toList();
    }

    protected ItemStack createCustomFuelItem(CommandSender sender, String customFuelId, int amount) {
        if (config.customFuels().isEmpty()) {
            throw new InfoLevelException("&cNo custom fuels loaded");
        }

        val customFuel = config.customFuels().get(customFuelId);
        if (customFuel == null) {
            throw new InfoLevelException("&cInvalid custom fuel id &6" + customFuelId);
        }

        val item = new ItemStack(customFuel.getItem());
        if (customFuel.isGlowing()) {
            itemDataProvider.setString(item, ItemDataKey.ENCHANTMENT, null);
        }

        val itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            throw new IllegalCallerException("&cInvalid item meta");
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



    protected ItemStack createJetpackItem(CommandSender sender, String jetpackId, long fuelValue) {
        val jetpack = config.jetpacks().get(jetpackId);
        if (jetpack == null) {
            throw new InfoLevelException("&cJetpack &l" + jetpackId + " &cdidn't exist");
        }
        val item = new ItemStack(jetpack.getItem());

        val itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            throw new IllegalCallerException("&cInvalid item meta");
        }
        itemMeta.setDisplayName(jetpack.getDisplayName());
        itemMeta.setLore(jetpack.getLore().stream().map(v -> v.replace(Placeholder.FUEL, getDisplayFuel(jetpack).replace(Placeholder.FUEL_VALUE, String.valueOf(fuelValue)))).toList());
        itemMeta.setCustomModelData(jetpack.getCustomModelData());

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

        if (jetpack.getCustomModelData() != -1) {
            itemMeta.setCustomModelData(jetpack.getCustomModelData());
        }

        for (String enchantment : jetpack.getEnchantments()) {
            Try.run(() -> {
                val sp = enchantment.split(":");
                val enchantName = sp[0];
                val enchantLvl = Integer.parseInt(sp[1]);
                @SuppressWarnings("deprecation")
                val enchantmentObj = version.getServerVersion() > 16 ?
                        Enchantment.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase())) :
                        Enchantment.getByName(enchantName.toUpperCase());
                assert enchantmentObj != null;
                itemMeta.addEnchant(enchantmentObj, enchantLvl, true);
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
        if (customFuel == null) {
            fuelDisplay = StringUtils.capitalize(fuelDisplay.toLowerCase());
        }
        return fuelDisplay;
    }

}
