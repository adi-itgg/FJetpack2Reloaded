package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Land-claim lookup (GriefPrevention).
 * <p>
 * The claim handle is cached per player by the adapter, because the underlying lookup runs on every
 * flight tick and the API itself accepts the previous claim as a hint.
 */
public interface ClaimLookup {

    /** @return false when the owning plugin is absent or disabled */
    boolean active();

    /** @return null when the player stands outside every claim */
    @Nullable
    Claim claimAt(@NotNull Player player);

}
