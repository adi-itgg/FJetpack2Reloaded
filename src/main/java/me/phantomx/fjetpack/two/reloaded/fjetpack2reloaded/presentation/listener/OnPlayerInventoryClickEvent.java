package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener;

import lombok.Getter;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.JetpackRefill;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.PlayerRegistry;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight.FlightCheck;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.item.ItemTagCodec;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
    private final ItemTagCodec itemTags;
    private final JetpackRefill refill;
    private final Log log;

    public OnPlayerInventoryClickEvent(@NotNull PlayerRegistry players,
                                       @NotNull ItemTagCodec itemTags,
                                       @NotNull JetpackRefill refill,
                                       @NotNull MessageSink messages) {
        this.players = players;
        this.itemTags = itemTags;
        this.refill = refill;
        this.log = new Log(getClass(), messages);
    }

    public void onClick(@NotNull InventoryClickEvent e) {
        if (e instanceof InventoryCreativeEvent || !(e.getWhoClicked() instanceof Player player)) return;

        val cursorItem = e.getCursor() ;
        val slotItem = e.getCurrentItem();
        if (cursorItem == null || slotItem == null) return;
        log.log("cursorItem: %s - %s (%s)", cursorItem.getType().name(), slotItem.getType().name(), e.getSlotType().name());

        checkJetpack(e, cursorItem, slotItem);

        if (!refill.refill(player, slotItem, cursorItem, e.isLeftClick())) return;

        e.setCancelled(true);
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
            if (check == FlightCheck.NO_PERMISSION) NoPermissionLvlException.throwNow();
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
        if (!itemTags.isItemArmorFast(cursorItem) || !itemTags.isItemArmorFast(slotItem)) return;
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
