package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.flight;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Jetpack;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link FlightState} is supposed to be data only - no port, no decision, no side effect. That is
 * what makes it safe to hand to collaborators, so it is worth asserting rather than assuming.
 */
@DisplayName("FlightState")
class FlightStateTest {

    @Test
    @DisplayName("a fresh state is inactive and belongs to nobody yet")
    void freshStateIsIdle() {
        var state = new FlightState(stubPlayer());

        assertFalse(state.isActive());
        assertFalse(state.isStarting());
        assertNull(state.getJetpack(), "no jetpack until one is adopted");
        assertNull(state.getBurnTask(), "no tick until one is scheduled");
        assertEquals(0, state.getJetpackUniqueId());
    }

    @Test
    @DisplayName("it only ever gets and sets - no method reaches for a decision")
    void holdsNoBehaviour() {
        for (Method method : FlightState.class.getDeclaredMethods()) {
            assertTrue(
                    method.getName().startsWith("get")
                            || method.getName().startsWith("set")
                            || method.getName().startsWith("is"),
                    "FlightState must stay data only, found: " + method.getName()
            );
        }
    }

    @Test
    @DisplayName("the jetpack it holds is the one that was set")
    void keepsTheJetpackItIsGiven() {
        var state = new FlightState(stubPlayer());
        var jetpack = new Jetpack();

        state.setJetpack(jetpack);
        state.setActive(true);

        assertEquals(jetpack, state.getJetpack());
        assertTrue(state.isActive());
    }

    private static Player stubPlayer() {
        return (Player) Proxy.newProxyInstance(
                FlightStateTest.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> null);
    }

}
