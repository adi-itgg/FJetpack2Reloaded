package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/** An island, as much of it as the use cases care about. */
public interface Island {

    @NotNull
    UUID id();

    boolean flagEnabled(@NotNull String flag);

    boolean hasPrivilege(@NotNull Player player, @NotNull String privilege);

}
