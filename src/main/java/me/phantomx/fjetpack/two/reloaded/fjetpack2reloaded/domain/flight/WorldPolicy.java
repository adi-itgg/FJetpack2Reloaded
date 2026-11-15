package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight;

import java.util.Collection;

/**
 * "May this jetpack be used here?" - the two world lists of one jetpack and nothing else.
 * <p>
 * A blocked world wins over an allowed one: a world named in both is refused, so the list that
 * tightens access is the one that decides and a misconfiguration fails closed. An empty allowed list
 * means no restriction at all, which is what every jetpack was before that list existed.
 */
public record WorldPolicy(Collection<String> blockedWorlds, Collection<String> allowedWorlds) {

    /** World names are compared as they are, case included - the same rule the block list always had. */
    public WorldAccess check(String worldName) {
        if (blockedWorlds.contains(worldName)) return WorldAccess.BLOCKED;
        if (!allowedWorlds.isEmpty() && !allowedWorlds.contains(worldName)) return WorldAccess.NOT_ALLOWED;
        return WorldAccess.ALLOWED;
    }

}
