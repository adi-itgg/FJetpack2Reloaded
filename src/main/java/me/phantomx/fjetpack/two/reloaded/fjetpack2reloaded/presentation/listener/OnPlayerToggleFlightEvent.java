package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.PlayerRegistry;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight.FlightCheck;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.NoPermissionLvlException;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class OnPlayerToggleFlightEvent {

    private final PlayerRegistry players;
    private final Log log;

    public OnPlayerToggleFlightEvent(@NotNull PlayerRegistry players, @NotNull MessageSink messages) {
        this.players = players;
        this.log = new Log(getClass(), messages);
    }

    public void onToggle(@NotNull PlayerToggleFlightEvent e) {
        val fj2RPlayer = players.of(e.getPlayer());
        log.debug("toggleFlight: player=%s flying=%s active=%s jetpack=%s",
                e.getPlayer().getName(), e.isFlying(), fj2RPlayer.isActive(),
                fj2RPlayer.getJetpack() == null ? "null" : fj2RPlayer.getJetpack().getId());
        if (!fj2RPlayer.isActive()) return;
        if (fj2RPlayer.getJetpack() != null && fj2RPlayer.getJetpack().isRunInOffHandOnly()) {
            val offHandItem = e.getPlayer().getInventory().getItemInOffHand();
            val check = fj2RPlayer.isJetpack(offHandItem, false);
            log.debug("toggleFlight: offhand item=%s check=%s", Log.item(offHandItem), check);
            if (check == FlightCheck.NO_PERMISSION) NoPermissionLvlException.throwNow();
            if (check == FlightCheck.NOT_A_JETPACK) fj2RPlayer.turnOffDetached();
            return;
        }
        AtomicBoolean jetpackExist = new AtomicBoolean(false);
        fj2RPlayer.updateActiveJetpackArmorEquipment(item -> {
            jetpackExist.set(true);
            return item;
        });
        log.debug("toggleFlight: worn jetpack found=%s", jetpackExist.get());
        if (jetpackExist.get()) return;
        log.debug("toggleFlight: no worn jetpack -> turnOffDetached");
        fj2RPlayer.turnOffDetached();
    }

}
