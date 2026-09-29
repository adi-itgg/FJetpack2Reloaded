package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** A land claim, as much of it as the use cases care about. */
public interface Claim {

    @NotNull
    UUID ownerId();

}
