package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.event;

import lombok.Getter;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.Configs;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.FJ2RPlayer;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.logging.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemMetaData;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Permissions;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class OnPlayerInventoryClickEvent {

    @Getter(lazy = true)
    private static final ItemStack ITEM_AIR = new ItemStack(Material.AIR);

    public static void onClick(@NotNull InventoryClickEvent e) {
        if (e instanceof InventoryCreativeEvent || !(e.getWhoClicked() instanceof Player player)) return;

        val cursorItem = e.getCursor() ;
        val slotItem = e.getCurrentItem();
        if (cursorItem == null || slotItem == null) return;
        Log.log("cursorItem: %s - %s (%s)", cursorItem.getType().name(), slotItem.getType().name(), e.getSlotType().name());

        checkJetpack(e, cursorItem, slotItem);

        if (cursorItem.getType() == Material.AIR || slotItem.getType() == Material.AIR) return;

        // handle refill fuel
        if (!slotItem.hasItemMeta() || slotItem.getType() == Material.AIR || slotItem.getAmount() == 0) return;
        if (!e.isLeftClick() && !e.isRightClick() && e.getClick() == ClickType.WINDOW_BORDER_LEFT && e.getClick() == ClickType.WINDOW_BORDER_RIGHT) return;
        val jetpackId = ItemMetaData.getJetpackID(slotItem, "");
        val jetpack = Configs.getJetpacksLoaded().get(jetpackId);
        if (jetpack == null) return;

        val customFuel = jetpack.getFuel().getCustomFuel();
        if (customFuel != null) {
            if (customFuel.getItem() != cursorItem.getType()
                    || !ItemMetaData.getCustomFuelID(cursorItem).equals(customFuel.getId())
            ) return;
            if (!Permissions.hasRawPermission(player, customFuel.getPermission()))
                NoPermissionLvlException.send();
        }
        if (customFuel == null)
            if (jetpack.getFuel().getItem() != cursorItem.getType()) return;

        if (!Permissions.hasRawPermission(player, jetpack.getPermission(Permissions.PERMISSION_REFILL_FUEL_SUFFIX)))
            NoPermissionLvlException.send();

        e.setCancelled(true);

        val addFuelAmount = e.isLeftClick() ? cursorItem.getAmount() : 1;
        var jpFuel = ItemMetaData.getFuelValue(slotItem);
        if (jpFuel == null) jpFuel = 0L;
        val fuel = jpFuel + addFuelAmount;

        val item = ItemMetaData.setFuelValue(slotItem, fuel);
        FJ2RPlayer.updateDisplayItem(jetpack, item, fuel);

        if (e.isLeftClick()) {
            player.setItemOnCursor(getITEM_AIR());
            return;
        }
        cursorItem.setAmount(cursorItem.getAmount() - 1);
        player.setItemOnCursor(cursorItem);
    }

    private static void checkJetpack(@NotNull InventoryClickEvent e, @NotNull ItemStack cursorItem, @NotNull ItemStack slotItem) {
        val fj2RPlayer = FJ2RPlayer.getAsFJ2RPlayer((Player) e.getWhoClicked());
        // check if player is flying using jetpack and check jetpack is exists from offhand
        if (fj2RPlayer.isActive() && fj2RPlayer.getJetpack() != null && fj2RPlayer.getJetpack().isRunInOffHandOnly()) {
            if (slotItem.getType() == Material.AIR || e.getSlotType() != InventoryType.SlotType.QUICKBAR) return;
            val offHandItem = fj2RPlayer.getPlayer().getInventory().getItemInOffHand();
            if (!fj2RPlayer.isJetpack(offHandItem, false)) {
                fj2RPlayer.turnOffDetached();
                return;
            }
            if (!offHandItem.isSimilar(slotItem)) return;
            fj2RPlayer.turnOffDetached();
            return;
        }
        // check if player is flying using jetpack and check jetpack is exists from equipment
        if (fj2RPlayer.isActive() || e.getSlotType() != InventoryType.SlotType.ARMOR) return;
        if (!ItemMetaData.isItemArmorFast(cursorItem) || !ItemMetaData.isItemArmorFast(slotItem)) return;
        if (ItemMetaData.isActiveJetpack(slotItem, fj2RPlayer.getJetpackUniqueId())) {
            fj2RPlayer.turnOffDetached();
            return;
        }
        AtomicBoolean jetpackExist = new AtomicBoolean(false);
        fj2RPlayer.updateActiveJetpackArmorEquipment(item -> {
            jetpackExist.set(true);
            return item;
        });
        if (!jetpackExist.get()) fj2RPlayer.turnOffDetached();
    }

}
