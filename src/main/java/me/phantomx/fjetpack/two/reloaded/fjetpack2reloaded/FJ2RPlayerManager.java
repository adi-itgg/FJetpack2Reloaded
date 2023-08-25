package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.model.NewFJ2RPlayer;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.reload.NewFJetpack2Reloaded;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class FJ2RPlayerManager {


    public static NewFJ2RPlayer get(@NotNull Player player) {
        var activePlayer = NewFJetpack2Reloaded.getInstance().getFJ2RPlayers().get(player.getUniqueId());
        if (activePlayer != null) {
            return activePlayer;
        }
        activePlayer = new NewFJ2RPlayer(player);
        NewFJetpack2Reloaded.getInstance().getFJ2RPlayers().put(player.getUniqueId(), activePlayer);
        return activePlayer;
    }

    public static void turnOff(NewFJ2RPlayer player) {

    }


}
