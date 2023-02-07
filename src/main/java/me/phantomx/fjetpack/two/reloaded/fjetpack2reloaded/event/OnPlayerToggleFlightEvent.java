package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.event;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.Configs;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.FJ2RPlayer;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class OnPlayerToggleFlightEvent {

    public static void onToggle(@NotNull PlayerToggleFlightEvent e) {
        val fj2RPlayer = FJ2RPlayer.getAsFJ2RPlayer(e.getPlayer());
        if (!fj2RPlayer.isActive()) return;
        AtomicBoolean jetpackExist = new AtomicBoolean(false);
        fj2RPlayer.updateActiveJetpackArmorEquipment(item -> {
            jetpackExist.set(true);
            return item;
        });
        if (jetpackExist.get()) return;
        fj2RPlayer.turnOff();
        Messages.sendMessage(e.getPlayer(), Configs.getMessage().getDetached());
    }

}
