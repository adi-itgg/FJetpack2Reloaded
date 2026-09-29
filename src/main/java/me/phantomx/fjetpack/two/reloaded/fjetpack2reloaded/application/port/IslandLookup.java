package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Island lookup (SuperiorSkyblock2). */
public interface IslandLookup {

    /** @return false when the owning plugin is absent or disabled */
    boolean active();

    /** Name this plugin registers both its island flag and its island privilege under. */
    @NotNull
    String privilegeName();

    /** @return null when the player stands outside every island */
    @Nullable
    Island islandAt(@NotNull Player player);

}
