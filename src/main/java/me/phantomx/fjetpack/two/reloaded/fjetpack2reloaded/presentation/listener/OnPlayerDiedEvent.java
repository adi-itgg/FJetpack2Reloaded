package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.PlayerRegistry;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight.ItemDisposition;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Permissions;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
public class OnPlayerDiedEvent {

    private final PlayerRegistry players;
    private final YamlConfigRepository configs;
    private final MessageSink messages;

    public OnPlayerDiedEvent(@NotNull PlayerRegistry players,
                             @NotNull YamlConfigRepository configs,
                             @NotNull MessageSink messages) {
        this.players = players;
        this.configs = configs;
        this.messages = messages;
    }

    public void onDied(@NotNull PlayerDeathEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) return;

        val player = (Player) e.getEntity();
        val fj2RPlayer = players.of(player);
        if (!fj2RPlayer.isActive()) return;
        if (fj2RPlayer.getJetpack() == null || fj2RPlayer.getJetpack().getOnDeath() == ItemDisposition.NONE) return;
        if (Permissions.hasRawPermission(player, fj2RPlayer.getJetpack().getPermission(Permissions.PERMISSION_KEEP_ON_DEATH_SUFFIX))) return;

        fj2RPlayer.turnOff();

        if (fj2RPlayer.getJetpack().isRunInOffHandOnly()) {
            val offHandItem = player.getInventory().getItemInOffHand();
            onDeathEvent(offHandItem, player, fj2RPlayer.getJetpack());
            return;
        }

        val equipment = player.getEquipment();
        if (equipment == null) return;

        fj2RPlayer.updateActiveJetpackArmorEquipment(item -> onDeathEvent(item, player, fj2RPlayer.getJetpack()));
    }

    private @Nullable ItemStack onDeathEvent(@NotNull ItemStack item, @NotNull Player player, @NotNull Jetpack jetpack) {
        return switch (jetpack.getOnDeath()) {
            case NONE -> item;
            case REMOVE -> {
                messages.send(player, configs.message().getOnDeathRemoved());
                yield null;
            }
            case DROP -> {
                player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
                messages.send(player, configs.message().getOnDeathDropped());
                yield null;
            }
        };
    }

}
