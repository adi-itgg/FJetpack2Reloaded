package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The plugin tags stored on an {@link ItemStack}.
 * <p>
 * Writing is deliberately narrow: use cases get the operations they need, not arbitrary tag access.
 * The tag keys themselves stay an implementation detail of the codec.
 */
public interface ItemTags {

    /** New per-activation id for the jetpack a player is currently wearing. */
    int newItemId();

    @NotNull
    ItemStack setJetpack(@NotNull ItemStack stack, @Nullable String id);

    @NotNull
    String getJetpackID(@NotNull ItemStack stack, @NotNull String defaultValue);

    @NotNull
    ItemStack setCustomFuelID(@NotNull ItemStack stack, @Nullable String id);

    @NotNull
    String getCustomFuelID(@NotNull ItemStack stack);

    @NotNull
    ItemStack setFuelValue(@NotNull ItemStack stack, @Nullable Long value);

    /** @return null when the stack carries no fuel tag at all */
    @Nullable
    Long getFuelValue(@NotNull ItemStack stack);

    @NotNull
    ItemStack setActiveJetpack(@NotNull ItemStack stack, int id);

    boolean isActiveJetpack(@NotNull ItemStack stack, int id);

    /** Removes a tag; {@code null} as the value does the same thing. */
    @NotNull
    ItemStack clearTag(@NotNull ItemStack stack, @NotNull String key);

    boolean isNotItemArmor(@NotNull ItemStack stack);

    boolean isItemArmorFast(@NotNull ItemStack stack);

}
