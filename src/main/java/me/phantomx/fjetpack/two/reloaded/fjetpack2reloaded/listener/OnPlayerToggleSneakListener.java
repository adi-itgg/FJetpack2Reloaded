package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.listener;

import io.avaje.inject.Component;
import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.FJConfig;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.ItemDataProvider;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.JetpackItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc.FJPlayerManager;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

@Component
@RequiredArgsConstructor
public class OnPlayerToggleSneakListener implements Listener {

    private static final EquipmentSlot[] EQUIPMENT_SLOTS = EquipmentSlot.values();

    private final FJConfig config;
    private final ItemDataProvider itemDataProvider;
    private final FJPlayerManager playerManager;
    private final JetpackItemFactory itemFactory;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEvent(PlayerToggleSneakEvent e) {
        var player = e.getPlayer();
        if (!e.isSneaking()) {
            return;
        }

        var entity = (Entity) player;
        if (!entity.isOnGround()) {
            return;
        }

        var fjPlayer = playerManager.getOrCreate(player);

        if (fjPlayer.fuelBurn().active()) { // turn off
            playerManager.deactivate(fjPlayer);
            return;
        }

        // activate jetpack from item offhand
        var offHandItem = player.getInventory().getItemInOffHand();
        var jetpack = itemFactory.getJetpack(offHandItem);
        if (jetpack != null && jetpack.isRunInOffHandOnly()) {
            playerManager.activate(fjPlayer, jetpack, offHandItem, EquipmentSlot.OFF_HAND);
            return;
        }

        // activate jetpack from equipment inventory
        var equipment = player.getEquipment();
        if (equipment == null) {
            return;
        }

        for (EquipmentSlot equipmentSlot : EQUIPMENT_SLOTS) {
            var item = player.getInventory().getItem(equipmentSlot);
            if (item == null) {
                continue;
            }
            jetpack = itemFactory.getJetpack(item);
            if (jetpack == null) {
                continue;
            }
            playerManager.activate(fjPlayer, jetpack, item, equipmentSlot);
            return;
        }



    }

}
