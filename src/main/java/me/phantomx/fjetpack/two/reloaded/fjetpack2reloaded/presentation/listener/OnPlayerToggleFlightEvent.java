package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.PlayerRegistry;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight.FlightCheck;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class OnPlayerToggleFlightEvent {

    private final PlayerRegistry players;

    public OnPlayerToggleFlightEvent(@NotNull PlayerRegistry players) {
        this.players = players;
    }

    public void onToggle(@NotNull PlayerToggleFlightEvent e) {
        val fj2RPlayer = players.of(e.getPlayer());
        if (!fj2RPlayer.isActive()) return;
        if (fj2RPlayer.getJetpack() != null && fj2RPlayer.getJetpack().isRunInOffHandOnly()) {
            val offHandItem = e.getPlayer().getInventory().getItemInOffHand();
            val check = fj2RPlayer.isJetpack(offHandItem, false);
            if (check == FlightCheck.NO_PERMISSION) NoPermissionLvlException.throwNow();
            if (check == FlightCheck.NOT_A_JETPACK) fj2RPlayer.turnOffDetached();
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
