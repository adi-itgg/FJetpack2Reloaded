package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application;

import jakarta.inject.Singleton;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.FlightSession;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.dto.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook.GriefPrevention;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook.SuperiorSkyblock;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util.Permissions;
import me.ryanhamshire.GriefPrevention.Claim;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Third-party protection checks (GriefPrevention claims, SuperiorSkyblock islands).
 * <p>
 * Answers the question and messages the player; the claim/island cache lives on the session.
 */
@Singleton
public class ProtectionGuard {

    private final YamlConfigRepository configs;
    private final MessageSink messages;

    public ProtectionGuard(@NotNull YamlConfigRepository configs, @NotNull MessageSink messages) {
        this.configs = configs;
        this.messages = messages;
    }

    public boolean allowsGriefPrevention(@NotNull FlightSession session, @NotNull Jetpack jetpack) {
        val player = session.getPlayer();
        if (Permissions.isAdminOrOp(player)) return true;
        val griefPrevention = jetpack.getGriefPrevention();
        if (griefPrevention == null || !griefPrevention.isEnable() || !GriefPrevention.isActive()) return true;
        @Nullable val claim = GriefPrevention.get().dataStore.getClaimAt(player.getLocation(),
                true,
                true,
                session.getGriefPreventionClaim()
        );
        if (griefPrevention.isAllowInsideAllClaim()) {
            if (claim != null) {
                session.setGriefPreventionClaim(claim);
                return true;
            }
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
        if (claim.getOwnerID() != player.getUniqueId()) {
            messages.send(player, configs.message().getNoPermission());
            return false;
        }
        session.setGriefPreventionClaim(claim);
        return true;
    }

    public boolean allowsSuperiorSkyblock(@NotNull FlightSession session, @NotNull Jetpack jetpack) {
        val player = session.getPlayer();
        if (!SuperiorSkyblock.isActive() || Permissions.isAdminOrOp(player)) return true;
        val superiorSkyblock2 = jetpack.getSuperiorSkyblock2();
        if (superiorSkyblock2 == null || !superiorSkyblock2.isEnable()) return true;
        // flag check
        if (superiorSkyblock2.isAllowBypassFlag() && Permissions.hasRawPermission(player,
                jetpack.getPermission(Permissions.PERMISSION_BYPASS_SUPERIOR_SKYBLOCK_FLAG)
        )) return true;
        val island = SuperiorSkyblockAPI.getIslandAt(player.getLocation());
        if (island == null) {
            messages.send(player, configs.message().getSuperiorSkyblock2OutsideIsland());
            return true;
        }
        if (!island.hasSettingsEnabled(IslandFlag.getByName(SuperiorSkyblock.FLAG_PRIVILEGE))) {
            messages.send(player, configs.message().getSuperiorSkyblock2NoFlag());
            return false;
        }
        // privilege check
        if (superiorSkyblock2.isAllowBypassPrivilege() && Permissions.hasRawPermission(player,
                jetpack.getPermission(Permissions.PERMISSION_BYPASS_SUPERIOR_SKYBLOCK_PRIVILEGE)
        )) return true;
        val hasPerm = island.hasPermission(player, IslandPrivilege.getByName(SuperiorSkyblock.FLAG_PRIVILEGE));
        if (!hasPerm)
            messages.send(player, configs.message().getSuperiorSkyblock2NoPermission());
        if (hasPerm)
            session.setSuperiorSkyblock2IslandUUID(island.getUniqueId());
        return hasPerm;
    }

}
