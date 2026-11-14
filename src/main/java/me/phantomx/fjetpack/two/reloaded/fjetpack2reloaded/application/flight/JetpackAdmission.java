package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.flight;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Permissions;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ProtectionGuard;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ItemTags;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight.FlightCheck;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * "May this stack be used as a jetpack by this player, right now?"
 * <p>
 * Every refusal branch also tells the player why, so a caller only has to act on the returned check.
 * Answers only - it writes nothing on the session, which is what lets the session ask this question
 * about a stack it has not adopted.
 */
final class JetpackAdmission {

    private final ConfigRepository configs;
    private final MessageSink messages;
    private final ItemTags itemTags;
    private final ProtectionGuard protection;
    private final Log log;

    JetpackAdmission(@NotNull ConfigRepository configs,
                     @NotNull MessageSink messages,
                     @NotNull ItemTags itemTags,
                     @NotNull ProtectionGuard protection) {
        this.configs = configs;
        this.messages = messages;
        this.itemTags = itemTags;
        this.protection = protection;
        this.log = new Log(getClass(), messages);
    }

    /**
     * @param stack the stack to judge
     * @param inUse the jetpack the session already adopted; a different id is refused
     */
    @NotNull Verdict judge(@NotNull Player player, @Nullable ItemStack stack, @Nullable Jetpack inUse) {
        log.debug("admission: player=%s stack=%s inUse=%s world=%s",
                player.getName(), Log.item(stack), inUse == null ? "null" : inUse.getId(), player.getWorld().getName());
        if (stack == null || stack.getType() == Material.AIR) {
            log.debug("admission: refused NOT_A_JETPACK - stack is null or AIR");
            return Verdict.refused(FlightCheck.NOT_A_JETPACK);
        }

        val id = itemTags.getJetpackID(stack, "");
        val jetpack = configs.jetpacks().get(id);
        log.debug("admission: item=%s jetpackTag=%s config=%s", Log.item(stack), id, jetpack == null ? "null" : jetpack.getId());
        if (jetpack == null) {
            log.debug("admission: refused NOT_A_JETPACK - no configured jetpack for tag=%s", id);
            return Verdict.refused(FlightCheck.NOT_A_JETPACK);
        }
        // an ItemsAdder jetpack carries whatever base material its item uses; the id tag above
        // already told us which jetpack this is
        if (!jetpack.usesItemsAdder() && stack.getType() != jetpack.getItem()) {
            log.debug("admission: refused NOT_A_JETPACK - material=%s configured=%s",
                    stack.getType(), jetpack.getItem());
            return Verdict.refused(FlightCheck.NOT_A_JETPACK);
        }
        if (inUse != null && !jetpack.getId().equals(inUse.getId())) {
            log.debug("admission: refused NOT_A_JETPACK - different jetpack in use=%s found=%s", inUse.getId(), jetpack.getId());
            return Verdict.refused(FlightCheck.NOT_A_JETPACK);
        }

        val permission = jetpack.getPermission();
        if (!Permissions.hasRawPermission(player, permission)) {
            log.debug("admission: refused NO_PERMISSION - player=%s permission=%s", player.getName(), permission);
            return Verdict.refused(FlightCheck.NO_PERMISSION);
        }

        val blockedWorld = jetpack.getBlockedWorlds().stream()
                .anyMatch(world -> player.getWorld().getName().equals(world));
        if (blockedWorld) {
            log.debug("admission: refused BLOCKED - world=%s is in blockedWorlds=%s",
                    player.getWorld().getName(), jetpack.getBlockedWorlds());
            messages.send(player, configs.message().getBlockedWorlds());
            return Verdict.refused(FlightCheck.BLOCKED);
        }

        if (!protection.allowsSuperiorSkyblock(player, jetpack)) {
            log.debug("admission: refused BLOCKED - SuperiorSkyblock denied player=%s jetpack=%s", player.getName(), jetpack.getId());
            return Verdict.refused(FlightCheck.BLOCKED);
        }
        if (!protection.allowsGriefPrevention(player, jetpack)) {
            log.debug("admission: refused BLOCKED - GriefPrevention denied player=%s jetpack=%s", player.getName(), jetpack.getId());
            return Verdict.refused(FlightCheck.BLOCKED);
        }

        log.debug("admission: ALLOWED player=%s jetpack=%s", player.getName(), jetpack.getId());
        return Verdict.allowed(jetpack);
    }

    /** Either a refusal, or the jetpack the stack turned out to be. */
    record Verdict(@NotNull FlightCheck check, @Nullable Jetpack jetpack) {

        static @NotNull Verdict refused(@NotNull FlightCheck check) {
            return new Verdict(check, null);
        }

        static @NotNull Verdict allowed(@NotNull Jetpack jetpack) {
            return new Verdict(FlightCheck.ALLOWED, jetpack);
        }

    }

}
