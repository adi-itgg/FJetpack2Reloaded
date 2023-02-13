package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.data.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.JetpackEvent;
import org.bukkit.Material;
import org.bukkit.permissions.Permission;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class Jetpack {

    private @NonNull String id;
    private @NonNull String displayName;
    private @NonNull Permission permission;
    private @NonNull Material item;
    private @Nullable ItemColor itemColor;
    private @NonNull List<String> lore = Collections.emptyList();
    private @NonNull List<String> flags = Collections.emptyList();
    private @NonNull List<String> enchantments = Collections.emptyList();
    private @NonNull List<String> blockedWorlds = Collections.emptyList();
    private @NonNull JetpackEvent onEmptyFuel = JetpackEvent.NONE;
    private @NonNull JetpackEvent onDeath = JetpackEvent.NONE;
    private @NonNull Fuel fuel;
    private @Nullable ParticleEffect particle;
    private @Nullable GriefPrevention griefPrevention;
    private @Nullable SuperiorSkyblock2 superiorSkyblock2;
    private @NonNull Float speed = 1.1f;
    private int customModelData = -1;
    private boolean unbreakable;
    private boolean runInOffHandOnly;

    public String getPermission(@NotNull String child) {
        return getPermission().getName() + child;
    }

    @Override
    public String toString() {
        return "Jetpack{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                ", permission=" + permission.getName() +
                ", item=" + item +
                ", itemColor=" + itemColor +
                ", lore=" + lore +
                ", flags=" + flags +
                ", enchantments=" + enchantments +
                ", blockedWorlds=" + blockedWorlds +
                ", onEmptyFuel=" + onEmptyFuel +
                ", onDeath=" + onDeath +
                ", fuel=" + fuel +
                ", particle=" + particle +
                ", griefPrevention=" + griefPrevention +
                ", superiorSkyblock2=" + superiorSkyblock2 +
                ", speed=" + speed +
                ", customModelData=" + customModelData +
                ", unbreakable=" + unbreakable +
                ", runInOffHand=" + runInOffHandOnly +
                '}';
    }
}
