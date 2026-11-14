package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener;

import jakarta.inject.Singleton;
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

@Singleton
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
        if (cursorItem == null || slotItem == null) {
            log.log("click: player=%s cursorOrSlot is null, skipped", player.getName());
            return;
        }
        log.log("cursorItem: %s - %s (%s) player=%s left=%s shift=%s",
                cursorItem.getType().name(), slotItem.getType().name(), e.getSlotType().name(),
                player.getName(), e.isLeftClick(), e.isShiftClick());

        checkJetpack(e, cursorItem, slotItem);

        if (!refill.refill(player, slotItem, cursorItem, e.isLeftClick())) {
            log.log("click: not a refill, left to Bukkit");
            return;
        }

        log.log("click: refill accepted, cancelling event player=%s", player.getName());
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
        log.log("checkJetpack: player=%s active=%s jetpack=%s slotType=%s",
                e.getWhoClicked().getName(), fj2RPlayer.isActive(),
                fj2RPlayer.getJetpack() == null ? "null" : fj2RPlayer.getJetpack().getId(), e.getSlotType());
        // check if player is flying using jetpack and check jetpack is exists from offhand
        if (fj2RPlayer.isActive() && fj2RPlayer.getJetpack() != null && fj2RPlayer.getJetpack().isRunInOffHandOnly()) {
            if (slotItem.getType() == Material.AIR || e.getSlotType() != InventoryType.SlotType.QUICKBAR) return;
            val offHandItem = fj2RPlayer.getPlayer().getInventory().getItemInOffHand();
            val check = fj2RPlayer.isJetpack(offHandItem, false);
            log.log("checkJetpack: offhand check=%s", check);
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
            log.log("checkJetpack: clicked armor slot holds the active jetpack -> turnOffDetached");
            fj2RPlayer.turnOffDetached();
            return;
        }
        AtomicBoolean jetpackExist = new AtomicBoolean(false);
        fj2RPlayer.updateActiveJetpackArmorEquipment(item -> {
            jetpackExist.set(true);
            return item;
        });
        log.log("checkJetpack: worn jetpack still equipped=%s", jetpackExist.get());
        if (!jetpackExist.get()) fj2RPlayer.turnOffDetached();
    }

}
