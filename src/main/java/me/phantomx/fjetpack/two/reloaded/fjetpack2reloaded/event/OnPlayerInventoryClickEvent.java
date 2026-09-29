package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.event;

import lombok.Getter;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.PlayerRegistry;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight.FlightCheck;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemTagCodec;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.logging.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.MessageSink;
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

    private final PlayerRegistry players;
    private final ItemFactory items;
    private final ItemTagCodec itemTags;
    private final Log log;

    public OnPlayerInventoryClickEvent(@NotNull PlayerRegistry players,
                                       @NotNull ItemFactory items,
                                       @NotNull ItemTagCodec itemTags,
                                       @NotNull MessageSink messages) {
        this.players = players;
        this.items = items;
        this.itemTags = itemTags;
        this.log = new Log(getClass(), messages);
    }

    public void onClick(@NotNull InventoryClickEvent e) {
        if (e instanceof InventoryCreativeEvent || !(e.getWhoClicked() instanceof Player player)) return;

        val cursorItem = e.getCursor() ;
        val slotItem = e.getCurrentItem();
        if (cursorItem == null || slotItem == null) return;
        log.log("cursorItem: %s - %s (%s)", cursorItem.getType().name(), slotItem.getType().name(), e.getSlotType().name());

        checkJetpack(e, cursorItem, slotItem);

        if (cursorItem.getType() == Material.AIR || slotItem.getType() == Material.AIR) return;

        // handle refill fuel
        if (!slotItem.hasItemMeta() || slotItem.getType() == Material.AIR || slotItem.getAmount() == 0) return;
        if (!e.isLeftClick() && !e.isRightClick() && e.getClick() == ClickType.WINDOW_BORDER_LEFT && e.getClick() == ClickType.WINDOW_BORDER_RIGHT) return;
        val jetpack = items.jetpackOf(slotItem);
        if (jetpack == null) return;

        val customFuel = jetpack.getFuel().getCustomFuel();
        if (customFuel != null) {
            if (customFuel.getItem() != cursorItem.getType()
                    || !itemTags.getCustomFuelID(cursorItem).equals(customFuel.getId())
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
        var jpFuel = itemTags.getFuelValue(slotItem);
        if (jpFuel == null) jpFuel = 0L;
        val fuel = jpFuel + addFuelAmount;

        val item = itemTags.setFuelValue(slotItem, fuel);
        ItemFactory.updateDisplayItem(jetpack, item, fuel);

        if (e.isLeftClick()) {
            player.setItemOnCursor(getITEM_AIR());
            return;
        }
        cursorItem.setAmount(cursorItem.getAmount() - 1);
        player.setItemOnCursor(cursorItem);
    }

    private void checkJetpack(@NotNull InventoryClickEvent e, @NotNull ItemStack cursorItem, @NotNull ItemStack slotItem) {
        val fj2RPlayer = players.of((Player) e.getWhoClicked());
        // check if player is flying using jetpack and check jetpack is exists from offhand
        if (fj2RPlayer.isActive() && fj2RPlayer.getJetpack() != null && fj2RPlayer.getJetpack().isRunInOffHandOnly()) {
            if (slotItem.getType() == Material.AIR || e.getSlotType() != InventoryType.SlotType.QUICKBAR) return;
            val offHandItem = fj2RPlayer.getPlayer().getInventory().getItemInOffHand();
            val check = fj2RPlayer.isJetpack(offHandItem, false);
            if (check == FlightCheck.NO_PERMISSION) NoPermissionLvlException.send();
            if (check == FlightCheck.NOT_A_JETPACK) {
                fj2RPlayer.turnOffDetached();
                return;
            }
            if (check != FlightCheck.ALLOWED) return;
            if (!offHandItem.isSimilar(slotItem)) return;
            fj2RPlayer.turnOffDetached();
            return;
        }
        // check if player is flying using jetpack and check jetpack is exists from equipment
        if (fj2RPlayer.isActive() || e.getSlotType() != InventoryType.SlotType.ARMOR) return;
        if (!ItemTagCodec.isItemArmorFast(cursorItem) || !ItemTagCodec.isItemArmorFast(slotItem)) return;
        if (itemTags.isActiveJetpack(slotItem, fj2RPlayer.getJetpackUniqueId())) {
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
