package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.flight;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.config.Jetpack;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight.ItemDisposition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Applies a {@link ItemDisposition} - {@code NONE}, {@code REMOVE} or {@code DROP} - to one jetpack
 * item, and says what happened.
 * <p>
 * Out-of-fuel and death both need this, which is why it is here rather than inside either caller. The
 * message per disposition differs between the two, so the caller passes which one to send.
 */
final class ItemDispositionApplier {

    private final ConfigRepository configs;
    private final MessageSink messages;

    ItemDispositionApplier(@NotNull ConfigRepository configs, @NotNull MessageSink messages) {
        this.configs = configs;
        this.messages = messages;
    }

    /**
     * @param disposition what to do with the item
     * @param onRemoved   message when the item is removed
     * @param onDropped   message when the item is dropped on the ground
     * @return the item to keep in the slot, or null to leave the slot empty
     */
    ItemStack apply(@NotNull Player player,
                    @NotNull ItemStack item,
                    @NotNull ItemDisposition disposition,
                    @NotNull String onRemoved,
                    @NotNull String onDropped) {
        return switch (disposition) {
            case NONE -> item;
            case REMOVE -> {
                messages.send(player, onRemoved);
                yield null;
            }
            case DROP -> {
                val dropped = player.getWorld().dropItemNaturally(player.getLocation(), item.clone());
                messages.send(player, onDropped);
                yield null;
            }
        };
    }

    /** Out-of-fuel messages, from message.yml. */
    String outOfFuelRemoved() {
        return configs.message().getOnEmptyFuelRemoved();
    }

    String outOfFuelDropped() {
        return configs.message().getOnEmptyFuelDropped();
    }

    /** Death messages, from message.yml. */
    String onDeathRemoved() {
        return configs.message().getOnDeathRemoved();
    }

    String onDeathDropped() {
        return configs.message().getOnDeathDropped();
    }

}
