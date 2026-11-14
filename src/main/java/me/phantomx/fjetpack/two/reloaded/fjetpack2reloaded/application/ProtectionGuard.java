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
    private final Log log;

    public ProtectionGuard(@NotNull ConfigRepository configs,
                           @NotNull MessageSink messages,
                           @NotNull ClaimLookup claims,
                           @NotNull IslandLookup islands) {
        this.configs = configs;
        this.messages = messages;
        this.claims = claims;
        this.islands = islands;
        this.log = new Log(getClass(), messages);
    }

    public boolean allowsGriefPrevention(@NotNull Player player, @NotNull Jetpack jetpack) {
        log.debug("griefPrevention: player=%s jetpack=%s", player.getName(), jetpack.getId());
        if (Permissions.isAdminOrOp(player)) {
            log.debug("griefPrevention: admin/op bypass");
            return true;
        }
        val griefPrevention = jetpack.getGriefPrevention();
        if (griefPrevention == null || !griefPrevention.isEnable() || !claims.active()) {
            log.debug("griefPrevention: not enforced (config=%s enabled=%s providerActive=%s)",
                    griefPrevention == null ? "null" : "present",
                    griefPrevention != null && griefPrevention.isEnable(), claims.active());
            return true;
        }

        val claim = claims.claimAt(player);
        if (griefPrevention.isAllowInsideAllClaim()) {
            if (claim != null) return true;
            messages.send(player, configs.message().getGriefPreventionOutsideClaim());
            return false;
        }
        if (claim == null) {
            log.debug("griefPrevention: player=%s has no claim here -> denied", player.getName());
            messages.send(player, configs.message().getGriefPreventionOutsideClaim());
            return false;
        }
        if (griefPrevention.isAllowBypassClaim() && Permissions.hasRawPermission(player,
                jetpack.getPermission(Permissions.PERMISSION_BYPASS_GRIEF_PREVENTION_CLAIM))
        ) {
            log.debug("griefPrevention: bypass claim permission -> allowed");
            return true;
        }
        if (!griefPrevention.isOnlyAllowInsideOwnClaim()) {
            log.debug("griefPrevention: claim is not own and onlyAllowInsideOwnClaim=false -> denied");
            messages.send(player, configs.message().getNoPermission());
            return false;
        }
        if (!claim.ownerId().equals(player.getUniqueId())) {
            log.debug("griefPrevention: claim owner=%s player=%s -> denied", claim.ownerId(), player.getUniqueId());
            messages.send(player, configs.message().getNoPermission());
            return false;
        }
        log.debug("griefPrevention: own claim -> allowed");
        return true;
    }

    public boolean allowsSuperiorSkyblock(@NotNull Player player, @NotNull Jetpack jetpack) {
        log.debug("superiorSkyblock: player=%s jetpack=%s providerActive=%s", player.getName(), jetpack.getId(), islands.active());
        if (!islands.active() || Permissions.isAdminOrOp(player)) {
            log.debug("superiorSkyblock: bypass (providerActive=%s admin=%s)", islands.active(), Permissions.isAdminOrOp(player));
            return true;
        }
        val superiorSkyblock2 = jetpack.getSuperiorSkyblock2();
        if (superiorSkyblock2 == null || !superiorSkyblock2.isEnable()) {
            log.debug("superiorSkyblock: not enforced");
            return true;
        }

        // flag check
        if (superiorSkyblock2.isAllowBypassFlag() && Permissions.hasRawPermission(player,
                jetpack.getPermission(Permissions.PERMISSION_BYPASS_SUPERIOR_SKYBLOCK_FLAG))
        ) return true;

        val island = islands.islandAt(player);
        if (island == null) {
            log.debug("superiorSkyblock: player=%s has no island -> allowed (outside)", player.getName());
            messages.send(player, configs.message().getSuperiorSkyblock2OutsideIsland());
            return true;
        }

        val privilege = islands.privilegeName();
        if (!island.flagEnabled(privilege)) {
            log.debug("superiorSkyblock: island flag=%s disabled -> denied", privilege);
            messages.send(player, configs.message().getSuperiorSkyblock2NoFlag());
            return false;
        }

        // privilege check
        if (superiorSkyblock2.isAllowBypassPrivilege() && Permissions.hasRawPermission(player,
                jetpack.getPermission(Permissions.PERMISSION_BYPASS_SUPERIOR_SKYBLOCK_PRIVILEGE))
        ) return true;

        val hasPerm = island.hasPrivilege(player, privilege);
        log.debug("superiorSkyblock: island privilege=%s hasPrivilege=%s", privilege, hasPerm);
        if (!hasPerm)
            messages.send(player, configs.message().getSuperiorSkyblock2NoPermission());
        return hasPerm;
    }

}
