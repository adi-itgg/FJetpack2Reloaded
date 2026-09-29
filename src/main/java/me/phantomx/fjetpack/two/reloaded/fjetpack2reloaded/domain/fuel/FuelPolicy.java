package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.fuel;

/**
 * Fuel arithmetic of one flight tick. Pure: no Bukkit, no I/O - unit testable.
 * <p>
 * Charging is sequential: the base cost is charged first and aborts the tick when it does not fit,
 * then the sprint surcharge is charged under the same rule.
 */
public final class FuelPolicy {

    private FuelPolicy() {
    }

    public static FuelSettlement settle(long fuel,
                                        long baseCost,
                                        long sprintCost,
                                        boolean bypassBase,
                                        boolean bypassSprint,
                                        boolean sprinting,
                                        long warnBelow) {
        if (!bypassBase && fuel < baseCost)
            return new FuelSettlement(FuelStop.BASE, fuel, false);

        var remaining = bypassBase ? fuel : fuel - baseCost;

        if (!bypassSprint && sprinting && remaining < sprintCost)
            return new FuelSettlement(FuelStop.SPRINT, remaining, false);

        if (!bypassSprint && sprinting)
            remaining -= sprintCost;

        return new FuelSettlement(FuelStop.NONE, remaining, warnBelow != -1 && remaining <= warnBelow);
    }

}
