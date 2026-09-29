package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.Claim;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ClaimLookup;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * GriefPrevention adapter for {@link ClaimLookup}.
 * <p>
 * The last claim of a player is remembered here because GriefPrevention takes it as a lookup hint
 * and the flight tick asks again every {@code burnRate} ticks. Keyed weakly, like the session
 * registry it mirrors.
 */
@Singleton
public class GriefPreventionClaims implements ClaimLookup {

    private final Map<UUID, me.ryanhamshire.GriefPrevention.Claim> lastClaims = new WeakHashMap<>();

    @Override
    public boolean active() {
        return GriefPrevention.isActive();
    }

    @Override
    public @Nullable Claim claimAt(@NotNull Player player) {
        val claim = GriefPrevention.get().dataStore
                .getClaimAt(player.getLocation(), true, true, lastClaims.get(player.getUniqueId()));
        if (claim == null) {
            lastClaims.remove(player.getUniqueId());
            return null;
        }
        lastClaims.put(player.getUniqueId(), claim);
        return new OwnedClaim(claim.getOwnerID());
    }

    private record OwnedClaim(@NotNull UUID ownerId) implements Claim {
    }

}
