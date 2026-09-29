package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ItemTags;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Refuelling a jetpack by clicking a fuel item onto it inside the inventory.
 */
public class JetpackRefill {

    private final ConfigRepository configs;
    private final ItemTags itemTags;
    private final ItemFactory items;

    public JetpackRefill(@NotNull ConfigRepository configs, @NotNull ItemTags itemTags, @NotNull ItemFactory items) {
        this.configs = configs;
        this.itemTags = itemTags;
        this.items = items;
    }

    /**
     * @return true when the click was a refill: the caller cancels the event and consumes the cursor
     */
    public boolean refill(@NotNull Player player, @NotNull ItemStack slotItem, @NotNull ItemStack cursorItem, boolean leftClick) {
        if (cursorItem.getType() == Material.AIR || slotItem.getType() == Material.AIR) return false;
        if (!slotItem.hasItemMeta() || slotItem.getAmount() == 0) return false;

        val jetpack = configs.jetpacks().get(itemTags.getJetpackID(slotItem, ""));
        if (jetpack == null) return false;

        val customFuel = jetpack.getFuel().getCustomFuel();
        if (customFuel != null) {
            if (customFuel.getItem() != cursorItem.getType()
                    || !itemTags.getCustomFuelID(cursorItem).equals(customFuel.getId())
            ) return false;
            if (!Permissions.hasRawPermission(player, customFuel.getPermission()))
                NoPermissionLvlException.throwNow();
        }
        if (customFuel == null)
            if (jetpack.getFuel().getItem() != cursorItem.getType()) return false;

        if (!Permissions.hasRawPermission(player, jetpack.getPermission(Permissions.PERMISSION_REFILL_FUEL_SUFFIX)))
            NoPermissionLvlException.throwNow();

        val addFuelAmount = leftClick ? cursorItem.getAmount() : 1;
        var jpFuel = itemTags.getFuelValue(slotItem);
        if (jpFuel == null) jpFuel = 0L;
        val fuel = jpFuel + addFuelAmount;

        val item = itemTags.setFuelValue(slotItem, fuel);
        items.updateDisplayItem(jetpack, item, fuel);
        return true;
    }

}
