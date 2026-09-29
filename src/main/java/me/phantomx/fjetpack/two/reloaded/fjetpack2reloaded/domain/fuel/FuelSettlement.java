package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.fuel;

/**
 * Result of charging one flight tick.
 *
 * @param stop  where charging stopped
 * @param fuel  fuel value to display/store: the value at the stop point, or the remaining fuel
 * @param warn  the configured low-fuel threshold was crossed
 */
public record FuelSettlement(FuelStop stop, long fuel, boolean warn) {

    public boolean allowed() {
        return stop == FuelStop.NONE;
    }

}
