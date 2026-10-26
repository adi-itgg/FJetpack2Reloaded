package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.flight;

import lombok.val;
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

    JetpackAdmission(@NotNull ConfigRepository configs,
                     @NotNull MessageSink messages,
                     @NotNull ItemTags itemTags,
                     @NotNull ProtectionGuard protection) {
        this.configs = configs;
        this.messages = messages;
        this.itemTags = itemTags;
        this.protection = protection;
    }

    /**
     * @param stack the stack to judge
     * @param inUse the jetpack the session already adopted; a different id is refused
     */
    @NotNull Verdict judge(@NotNull Player player, @Nullable ItemStack stack, @Nullable Jetpack inUse) {
        if (stack == null || stack.getType() == Material.AIR) return Verdict.refused(FlightCheck.NOT_A_JETPACK);

        val jetpack = configs.jetpacks().get(itemTags.getJetpackID(stack, ""));
        if (jetpack == null) return Verdict.refused(FlightCheck.NOT_A_JETPACK);
        if (stack.getType() != jetpack.getItem()) return Verdict.refused(FlightCheck.NOT_A_JETPACK);
        if (inUse != null && !jetpack.getId().equals(inUse.getId())) return Verdict.refused(FlightCheck.NOT_A_JETPACK);

        if (!Permissions.hasRawPermission(player, jetpack.getPermission()))
            return Verdict.refused(FlightCheck.NO_PERMISSION);

        val blockedWorld = jetpack.getBlockedWorlds().stream()
                .anyMatch(world -> player.getWorld().getName().equals(world));
        if (blockedWorld) {
            messages.send(player, configs.message().getBlockedWorlds());
            return Verdict.refused(FlightCheck.BLOCKED);
        }

        if (!protection.allowsSuperiorSkyblock(player, jetpack)) return Verdict.refused(FlightCheck.BLOCKED);
        if (!protection.allowsGriefPrevention(player, jetpack)) return Verdict.refused(FlightCheck.BLOCKED);

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
