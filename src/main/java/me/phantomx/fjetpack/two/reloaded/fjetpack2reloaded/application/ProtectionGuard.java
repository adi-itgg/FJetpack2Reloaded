package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ClaimLookup;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.IslandLookup;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Third-party protection checks (GriefPrevention claims, SuperiorSkyblock2 islands).
 * <p>
 * Answers the question and messages the player. Caching of claims and islands is the adapter's job,
 * so this class only asks whether the player may fly right now.
 */
@Singleton
public class ProtectionGuard {

    private final ConfigRepository configs;
    private final MessageSink messages;
    private final ClaimLookup claims;
    private final IslandLookup islands;

    public ProtectionGuard(@NotNull ConfigRepository configs,
                           @NotNull MessageSink messages,
                           @NotNull ClaimLookup claims,
                           @NotNull IslandLookup islands) {
        this.configs = configs;
        this.messages = messages;
        this.claims = claims;
        this.islands = islands;
    }

    public boolean allowsGriefPrevention(@NotNull Player player, @NotNull Jetpack jetpack) {
        if (Permissions.isAdminOrOp(player)) return true;
        val griefPrevention = jetpack.getGriefPrevention();
        if (griefPrevention == null || !griefPrevention.isEnable() || !claims.active()) return true;

        val claim = claims.claimAt(player);
        if (griefPrevention.isAllowInsideAllClaim()) {
            if (claim != null) return true;
            messages.send(player, configs.message().getGriefPreventionOutsideClaim());
            return false;
        }
        if (claim == null) {
            messages.send(player, configs.message().getGriefPreventionOutsideClaim());
            return false;
        }
        if (griefPrevention.isAllowBypassClaim() && Permissions.hasRawPermission(player,
                jetpack.getPermission(Permissions.PERMISSION_BYPASS_GRIEF_PREVENTION_CLAIM))
        ) return true;
        if (!griefPrevention.isOnlyAllowInsideOwnClaim()) {
            messages.send(player, configs.message().getNoPermission());
            return false;
        }
        if (!claim.ownerId().equals(player.getUniqueId())) {
            messages.send(player, configs.message().getNoPermission());
            return false;
        }
        return true;
    }

    public boolean allowsSuperiorSkyblock(@NotNull Player player, @NotNull Jetpack jetpack) {
        if (!islands.active() || Permissions.isAdminOrOp(player)) return true;
        val superiorSkyblock2 = jetpack.getSuperiorSkyblock2();
        if (superiorSkyblock2 == null || !superiorSkyblock2.isEnable()) return true;

        // flag check
        if (superiorSkyblock2.isAllowBypassFlag() && Permissions.hasRawPermission(player,
                jetpack.getPermission(Permissions.PERMISSION_BYPASS_SUPERIOR_SKYBLOCK_FLAG))
        ) return true;

        val island = islands.islandAt(player);
        if (island == null) {
            messages.send(player, configs.message().getSuperiorSkyblock2OutsideIsland());
            return true;
        }

        val privilege = islands.privilegeName();
        if (!island.flagEnabled(privilege)) {
            messages.send(player, configs.message().getSuperiorSkyblock2NoFlag());
            return false;
        }

        // privilege check
        if (superiorSkyblock2.isAllowBypassPrivilege() && Permissions.hasRawPermission(player,
                jetpack.getPermission(Permissions.PERMISSION_BYPASS_SUPERIOR_SKYBLOCK_PRIVILEGE))
        ) return true;

        val hasPerm = island.hasPrivilege(player, privilege);
        if (!hasPerm)
            messages.send(player, configs.message().getSuperiorSkyblock2NoPermission());
        return hasPerm;
    }

}
