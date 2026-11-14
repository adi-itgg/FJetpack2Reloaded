package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ItemTags;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Refuelling a jetpack by clicking a fuel item onto it inside the inventory.
 */
@Singleton
public class JetpackRefill {

    private final ConfigRepository configs;
    private final ItemTags itemTags;
    private final ItemFactory items;
    private final Log log;

    public JetpackRefill(@NotNull ConfigRepository configs, @NotNull ItemTags itemTags, @NotNull ItemFactory items,
                         @NotNull MessageSink messages) {
        this.configs = configs;
        this.itemTags = itemTags;
        this.items = items;
        this.log = new Log(getClass(), messages);
    }

    /**
     * @return the rewritten slot item when the click was a refill, or null otherwise. The caller must
     * write it back to the clicked slot: on the legacy NMS path every tag write returns a new stack.
     */
    public @Nullable ItemStack refill(@NotNull Player player, @NotNull ItemStack slotItem, @NotNull ItemStack cursorItem, boolean leftClick) {
        log.debug("refill: player=%s slot=%s cursor=%s leftClick=%s",
                player.getName(), Log.item(slotItem), Log.item(cursorItem), leftClick);
        if (cursorItem.getType() == Material.AIR || slotItem.getType() == Material.AIR) {
            log.debug("refill: skipped - slot or cursor is AIR");
            return null;
        }
        if (!slotItem.hasItemMeta() || slotItem.getAmount() == 0) {
            log.debug("refill: skipped - slot item has no meta or amount=0");
            return null;
        }

        val slotId = itemTags.getJetpackID(slotItem, "");
        val jetpack = configs.jetpacks().get(slotId);
        log.debug("refill: slotJetpackId=%s config=%s", slotId, jetpack == null ? "null" : jetpack.getId());
        if (jetpack == null) return null;

        val customFuel = jetpack.getFuel().getCustomFuel();
        if (customFuel != null) {
            log.debug("refill: customFuel id=%s item=%s cursor=%s", customFuel.getId(), customFuel.getItem(), cursorItem.getType());
            if (customFuel.getItem() != cursorItem.getType()
                    || !itemTags.getCustomFuelID(cursorItem).equals(customFuel.getId())
            ) {
                log.debug("refill: skipped - cursor is not the configured custom fuel");
                return null;
            }
            if (!Permissions.hasRawPermission(player, customFuel.getPermission())) {
                log.debug("refill: no permission %s", customFuel.getPermission());
                NoPermissionLvlException.throwNow();
            }
        }
        if (customFuel == null)
            if (jetpack.getFuel().getItem() != cursorItem.getType()) {
                log.debug("refill: skipped - cursor item=%s is not configured fuel=%s",
                        cursorItem.getType(), jetpack.getFuel().getItem());
                return null;
            }

        if (!Permissions.hasRawPermission(player, jetpack.getPermission(Permissions.PERMISSION_REFILL_FUEL_SUFFIX))) {
            log.debug("refill: no permission %s", jetpack.getPermission(Permissions.PERMISSION_REFILL_FUEL_SUFFIX));
            NoPermissionLvlException.throwNow();
        }

        val addFuelAmount = leftClick ? cursorItem.getAmount() : 1;
        var jpFuel = itemTags.getFuelValue(slotItem);
        if (jpFuel == null) jpFuel = 0L;
        val fuel = jpFuel + addFuelAmount;
        log.debug("refill: player=%s jetpackId=%s fuelOld=%d add=%d fuelNew=%d",
                player.getName(), jetpack.getId(), jpFuel, addFuelAmount, fuel);

        // Display first, fuel tag last: on the NMS path the second write is the one that returns the
        // stack carrying both changes.
        items.updateDisplayItem(jetpack, slotItem, fuel);
        val item = itemTags.setFuelValue(slotItem, fuel);
        log.debug("refill: done player=%s jetpackId=%s fuel=%d item=%s",
                player.getName(), jetpack.getId(), fuel, Log.item(item));
        return item;
    }

}
