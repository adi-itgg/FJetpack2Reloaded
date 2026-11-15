package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("World policy")
class WorldPolicyTest {

    @Test
    @DisplayName("an empty allow list restricts nothing, so old configs keep working")
    void emptyAllowListAllowsEveryWorld() {
        var policy = new WorldPolicy(List.of("blocked"), List.of());

        assertEquals(WorldAccess.ALLOWED, policy.check("overworld"));
        assertEquals(WorldAccess.BLOCKED, policy.check("blocked"));
    }

    @Test
    @DisplayName("a filled allow list is the only way in")
    void allowListIsExclusive() {
        var policy = new WorldPolicy(List.of(), List.of("sky"));

        assertEquals(WorldAccess.ALLOWED, policy.check("sky"));
        assertEquals(WorldAccess.NOT_ALLOWED, policy.check("nether"));
    }

    @Test
    @DisplayName("a blocked world wins over an allowed one")
    void blockedWorldWins() {
        assertEquals(WorldAccess.BLOCKED, new WorldPolicy(List.of("sky"), List.of("sky")).check("sky"));
    }

    @Test
    @DisplayName("names are case sensitive, as the block list always was")
    void namesAreCaseSensitive() {
        assertEquals(WorldAccess.NOT_ALLOWED, new WorldPolicy(List.of(), List.of("Sky")).check("sky"));
    }

}
