package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.flight;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ServerVersion;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ItemTags;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.UnaryOperator;

/**
 * The act of touching the armour a player is wearing.
 * <p>
 * "The active jetpack" is not a slot: the item is found by its tag, so a player may move it between
 * armour slots or into the off hand. Every caller that rewrites it - fuel display, out-of-fuel
 * disposition, death handling, a detach check - goes through here instead of open-coding the walk.
 */
final class WornJetpack {

    private final ItemTags itemTags;
    private final ServerVersion version;
    private final Log log;

    WornJetpack(@NotNull ItemTags itemTags, @NotNull ServerVersion version, @NotNull MessageSink messages) {
        this.itemTags = itemTags;
        this.version = version;
        this.log = new Log(getClass(), messages);
    }

    /**
     * Rewrites the worn jetpack in place.
     *
     * @param jetpack the jetpack in use, or null when none is selected
     * @param uniqueId the id this activation wrote into the item; see {@link ItemTags#isActiveJetpack}
     * @param action  the rewrite; null removes the item from the slot
     */
    void update(@NotNull Player player,
                @Nullable Jetpack jetpack,
                int uniqueId,
                @NotNull UnaryOperator<@Nullable ItemStack> action) {
        update(player, jetpack, uniqueId, null, action);
    }

    /**
     * Rewrites the worn jetpack, including the stack a caller just read but has not activated yet.
     * <p>
     * On the legacy NMS path every tag write returns a new {@link ItemStack}; the stack passed to
     * {@code isJetpack} is still the one in the equipment. Matching that original stack is what puts
     * the active-tag copy back into the slot.
     *
     * @param target the stack the caller wants replaced, or null to match only already-active items
     */
    void update(@NotNull Player player,
                @Nullable Jetpack jetpack,
                int uniqueId,
                @Nullable ItemStack target,
                @NotNull UnaryOperator<@Nullable ItemStack> action) {
        val equipment = player.getEquipment();
        if (equipment == null) {
            log.debug("worn: player=%s has no equipment", player.getName());
            return;
        }
        val contents = equipment.getArmorContents();
        log.debug("worn: player=%s uniqueId=%d jetpack=%s slots=%d target=%s", player.getName(), uniqueId,
                jetpack == null ? "null" : jetpack.getId(), contents.length, Log.item(target));
        val armor = new ItemStack[contents.length];
        val iterator = Arrays.stream(contents).iterator();
        var i = 0;
        var matched = false;
        while (iterator.hasNext()) {
            var item = iterator.next();
            val active = item != null && itemTags.isActiveJetpack(item, uniqueId);
            val targetMatch = target != null && item != null && item.isSimilar(target);
            if (log.isDebugEnabled())
                log.debug("worn: player=%s slot=%d item=%s active=%s targetMatch=%s",
                        player.getName(), i, Log.item(item), active, targetMatch);
            if (active || targetMatch) {
                if (version.getServerVersion() > 16) {
                    val itemMeta = item.getItemMeta();
                    if (itemMeta != null && jetpack != null) {
                        itemMeta.setUnbreakable(jetpack.isUnbreakable());
                        item.setItemMeta(itemMeta);
                    }
                }
                item = action.apply(item);
                matched = true;
                log.debug("worn: player=%s slot=%d rewritten to %s", player.getName(), i, Log.item(item));
            }
            armor[i] = item;
            i++;
        }
        equipment.setArmorContents(armor);
        if (target != null && !matched) {
            val inventory = player.getInventory();
            val offHand = inventory.getItemInOffHand();
            if (offHand != null && offHand.isSimilar(target)) {
                inventory.setItemInOffHand(action.apply(offHand));
                log.debug("worn: player=%s offhand rewritten", player.getName());
            }
        }
        log.debug("worn: player=%s equipment updated", player.getName());
    }

}
