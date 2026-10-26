package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook;

import jakarta.inject.Singleton;

/**
 * Whether this plugin already registered its island flag and privilege with SuperiorSkyblock2.
 * <p>
 * Two separate listeners need it: the one that registers on {@code PluginInitializeEvent} sets it,
 * and the hook lifecycle reads it to decide whether the hook needs a restart warning. Shared state on
 * purpose - it is the one fact that both sides have to agree on.
 */
@Singleton
public class SuperiorSkyblockHookState {

    private boolean registered;

    public boolean isRegistered() {
        return registered;
    }

    public void markRegistered() {
        registered = true;
    }

}
