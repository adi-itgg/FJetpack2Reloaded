package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.event;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.Configs;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemMetaData;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Version;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class OnPlayerDamagedEvent {

    @SuppressWarnings("deprecation")
    public static void onDamaged(@NotNull EntityDamageEvent e) {
        if (Version.getServerVersion() > 16 || !(e.getEntity() instanceof Player player)) return;

        val equipment = player.getEquipment();
        if (equipment == null) return;
        val size = equipment.getArmorContents().length;
        val armorArray = new ItemStack[size];
        val iterator = Arrays.stream(equipment.getArmorContents()).iterator();
        var update = false;
        var i = 0;
        while (iterator.hasNext()) {
            var item = iterator.next();
            if (item == null) {
                armorArray[i] = null;
                i++;
                continue;
            }
            val jetpackId = ItemMetaData.getJetpackID(item, "");
            val jetpack = Configs.getJetpacksLoaded().get(jetpackId);
            if (jetpack != null && jetpack.isUnbreakable()) {
                item.setDurability((short) 0);
                update = true;
            }
            armorArray[i] = item;
            i++;
        }
        if (!update) return;
        equipment.setArmorContents(armorArray);
    }

}
