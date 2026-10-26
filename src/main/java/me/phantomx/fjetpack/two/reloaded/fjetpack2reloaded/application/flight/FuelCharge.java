package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.flight;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Permissions;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.fuel.FuelPolicy;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.fuel.FuelSettlement;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Charges the fuel of one tick: it reads the bypass permissions and the sprint state, then asks the
 * pure {@link FuelPolicy} what that tick costs.
 * <p>
 * Separated from the session so the arithmetic stays free of a live player, and so the session only
 * has to decide what the settlement means.
 */
final class FuelCharge {

    /**
     * @param fuel      the fuel currently in the item
     * @param jetpack   the jetpack in use
     * @param sprinting whether the player is sprinting right now
     */
    @NotNull
    FuelSettlement settle(@NotNull Player player, long fuel, @NotNull Jetpack jetpack, boolean sprinting) {
        return FuelPolicy.settle(
                fuel,
                jetpack.getFuel().getCost(),
                jetpack.getFuel().getSprintCost(),
                Permissions.hasRawPermission(player, jetpack.getPermission() + Permissions.PERMISSION_BYPASS_FUEL_SUFFIX),
                Permissions.hasRawPermission(player, jetpack.getPermission() + Permissions.PERMISSION_BYPASS_FUEL_SPRINT_SUFFIX),
                sprinting,
                jetpack.getFuel().getWarnRunOutBelow()
        );
    }

}
