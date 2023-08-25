package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.reload;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.model.NewFJ2RPlayer;

import java.util.UUID;
import java.util.WeakHashMap;

public interface NewFJetpack2Reloaded {

    boolean isShutdown();

    int getUniqueId();

    WeakHashMap<UUID, NewFJ2RPlayer> getFJ2RPlayers();

    static NewFJetpack2Reloaded getInstance() {
        return NewFJetpack2ReloadedImpl.getInstance();
    }

}
