package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.flight;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ServerVersion;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ItemTags;
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

    WornJetpack(@NotNull ItemTags itemTags, @NotNull ServerVersion version) {
        this.itemTags = itemTags;
        this.version = version;
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
        val equipment = player.getEquipment();
        if (equipment == null) return;
        val armor = new ItemStack[equipment.getArmorContents().length];
        val iterator = Arrays.stream(equipment.getArmorContents()).iterator();
        var i = 0;
        while (iterator.hasNext()) {
            var item = iterator.next();
            if (item != null && itemTags.isActiveJetpack(item, uniqueId)) {
                if (version.getServerVersion() > 16) {
                    val itemMeta = item.getItemMeta();
                    if (itemMeta != null && jetpack != null) {
                        itemMeta.setUnbreakable(jetpack.isUnbreakable());
                        item.setItemMeta(itemMeta);
                    }
                }
                item = action.apply(item);
            }
            armor[i] = item;
            i++;
        }
        equipment.setArmorContents(armor);
    }

}
