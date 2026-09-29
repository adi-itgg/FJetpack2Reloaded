package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.fuel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The flight tick arithmetic: no Bukkit, no server.
 */
class FuelPolicyTest {

    private static final long BASE_COST = 1;
    private static final long SPRINT_COST = 2;

    private static FuelSettlement settle(long fuel, boolean bypassBase, boolean bypassSprint, boolean sprinting) {
        return FuelPolicy.settle(fuel, BASE_COST, SPRINT_COST, bypassBase, bypassSprint, sprinting, 5);
    }

    @Test
    void chargesTheBaseCost() {
        FuelSettlement settlement = settle(10, false, false, false);

        assertEquals(FuelStop.NONE, settlement.stop());
        assertEquals(9, settlement.fuel());
        assertFalse(settlement.warn());
    }

    @Test
    void refusesWhenTheBaseCostDoesNotFit() {
        FuelSettlement settlement = settle(0, false, false, false);

        assertEquals(FuelStop.BASE, settlement.stop());
        assertEquals(0, settlement.fuel(), "fuel at the stop point is reported, nothing is charged");
        assertFalse(settlement.allowed());
    }

    @Test
    void chargesTheSprintSurchargeOnTopOfTheBaseCost() {
        FuelSettlement settlement = settle(10, false, false, true);

        assertEquals(FuelStop.NONE, settlement.stop());
        assertEquals(7, settlement.fuel());
    }

    @Test
    void refusesWhenTheSprintSurchargeDoesNotFitAndKeepsThePostBaseCostFuel() {
        FuelSettlement settlement = settle(2, false, false, true);

        assertEquals(FuelStop.SPRINT, settlement.stop());
        assertEquals(1, settlement.fuel(), "the base cost was already charged, the surcharge was not");
    }

    @Test
    void bypassSkipsTheMatchingCharge() {
        assertEquals(10, settle(10, true, true, true).fuel(), "both charges bypassed");
        assertEquals(9, settle(10, false, true, true).fuel(), "only the base cost is charged");
        assertEquals(8, settle(10, true, false, true).fuel(), "only the sprint surcharge is charged");
    }

    @Test
    void warnsAtOrBelowTheThreshold() {
        assertTrue(FuelPolicy.settle(6, BASE_COST, SPRINT_COST, false, false, false, 5).warn(), "5 after charging");
        assertFalse(FuelPolicy.settle(7, BASE_COST, SPRINT_COST, false, false, false, 5).warn(), "6 after charging");
    }

    @Test
    void neverWarnsWhenTheThresholdIsDisabled() {
        assertFalse(FuelPolicy.settle(0, 0, 0, true, true, false, -1).warn());
    }

    @Test
    void bypassedChargesCanStillRunOutOnTheSprintSurcharge() {
        FuelSettlement settlement = settle(1, true, false, true);

        assertEquals(FuelStop.SPRINT, settlement.stop());
        assertEquals(1, settlement.fuel());
    }

}
