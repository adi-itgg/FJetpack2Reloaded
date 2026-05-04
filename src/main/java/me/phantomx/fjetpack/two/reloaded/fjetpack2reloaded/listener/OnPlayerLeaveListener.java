package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.listener;

import io.avaje.inject.Component;
import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc.FJPlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@Component
@RequiredArgsConstructor
public class OnPlayerLeaveListener implements Listener {

    private final FJPlayerManager playerManager;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEvent(PlayerQuitEvent e) {
        playerManager.cleanup(e.getPlayer().getUniqueId());
    }

}
