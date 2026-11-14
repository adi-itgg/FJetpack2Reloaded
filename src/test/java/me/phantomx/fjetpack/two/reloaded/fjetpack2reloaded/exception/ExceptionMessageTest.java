package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * These two exceptions are the only thing the entry points have left to explain a refusal, and the
 * wording is what a denial ends up showing. It used to be a constant string, so it is pinned here.
 */
@DisplayName("Exception messages")
class ExceptionMessageTest {

    @Test
    @DisplayName("a no-permission abort names the node and the action")
    void noPermissionCarriesNodeAndAction() {
        assertEquals("missing permission 'fjetpack2reloaded.set' while running /fj2r set",
                new NoPermissionLvlException("fjetpack2reloaded.set", "running /fj2r set").getMessage());
    }

    @Test
    @DisplayName("a no-permission abort without a resolved node still says so")
    void noPermissionWithoutNodeIsExplicit() {
        assertEquals("missing permission (not resolved)",
                new NoPermissionLvlException(null, null).getMessage());
    }

    @Test
    @DisplayName("an already-messaged abort repeats what the sender was told")
    void alreadyMessagedCarriesReason() {
        assertEquals("already messaged: jetpack id 'diamond' is not in jetpack.yml",
                new IMessageException("jetpack id 'diamond' is not in jetpack.yml").getMessage());
    }

}
