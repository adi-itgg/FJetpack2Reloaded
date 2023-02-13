package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.event;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.FJ2RPlayer;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class OnPlayerToggleFlightEvent {

    public static void onToggle(@NotNull PlayerToggleFlightEvent e) {
        val fj2RPlayer = FJ2RPlayer.getAsFJ2RPlayer(e.getPlayer());
        if (!fj2RPlayer.isActive()) return;
        if (fj2RPlayer.getJetpack() != null && fj2RPlayer.getJetpack().isRunInOffHandOnly()) {
            val offHandItem = e.getPlayer().getInventory().getItemInOffHand();
            if (!fj2RPlayer.isJetpack(offHandItem, false)) fj2RPlayer.turnOffDetached();
            return;
        }
        AtomicBoolean jetpackExist = new AtomicBoolean(false);
        fj2RPlayer.updateActiveJetpackArmorEquipment(item -> {
            jetpackExist.set(true);
            return item;
        });
        if (jetpackExist.get()) return;
        fj2RPlayer.turnOffDetached();
    }

}
