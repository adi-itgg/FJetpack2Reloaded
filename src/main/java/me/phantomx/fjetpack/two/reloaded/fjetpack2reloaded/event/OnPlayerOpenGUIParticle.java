package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.event;

import lombok.Getter;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Version;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OnPlayerOpenGUIParticle implements Listener, InventoryHolder {

    @Getter(lazy = true)
    private final @NotNull Inventory particleInventory = initializeGUI();

    private @NotNull Inventory initializeGUI() {
        var inventory = Bukkit.createInventory(this, Version.getServerVersion() > 8 ? Particle.values().length : Effect.values().length, "Particle");
        if (Version.getServerVersion() > 8) {
            for (Particle particle : Particle.values())
                addItem(inventory, particle.name());
            return inventory;
        }
        for (Effect effect : Effect.values())
            addItem(inventory, effect.name());
        return inventory;
    }

    private void addItem(Inventory inventory, String effectName) {
        val item = new ItemStack(Material.POTION);
        val meta = item.getItemMeta();
        if (item.hasItemMeta() && meta != null)
            meta.setDisplayName(Messages.translateColorCodes("&eEffect: &6&l" + effectName));
        item.setItemMeta(meta);
        inventory.addItem(item);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return getParticleInventory();
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent e) {
        // Check if the clicked inventory is the GUI
        if (e.getInventory().getHolder() != this) {
            return;
        }

        // Check which item was clicked and handle the event
        ItemStack clickedItem = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        if (clickedItem.getType() != Material.POTION) return;

        // do change particle when clicked


    }


}
