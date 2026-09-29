package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.fuel;

/** Where a fuel settlement stopped. */
public enum FuelStop {
    /** Fuel was charged normally. */
    NONE,
    /** Not enough fuel for the base cost of this flight tick. */
    BASE,
    /** Not enough fuel for the sprint surcharge. */
    SPRINT
}
