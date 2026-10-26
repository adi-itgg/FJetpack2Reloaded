package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.BukkitListener;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook.IslandChangeRolePrivilegeListener;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook.IslandDisableFlagListener;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook.SuperiorSkyblockInitializeListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Registers every {@link BukkitListener} of the plugin.
 * <p>
 * The list is explicit rather than discovered from the container: a listener that is not on it is a
 * bug that a test can see (see {@code ListenerRegistrationTest}), while a container lookup would
 * happily register anything that happens to implement the interface, including a test double.
 * <p>
 * Uses {@link PluginManager#registerEvents} directly instead of the port's default method so a test
 * can observe the registration without a running server.
 */
@Singleton
public class ListenerRegistrar {

    private final List<BukkitListener> listeners;

    @Inject
    public ListenerRegistrar(@NotNull OnPlayerToggleSneakListener toggleSneak,
                             @NotNull OnPlayerToggleFlyListener toggleFly,
                             @NotNull OnPlayerDeathListener death,
                             @NotNull OnPlayerInventoryClickListener inventoryClick,
                             @NotNull OnPlayerDamagedListener damaged,
                             @NotNull OnPlayerJoinListener join,
                             @NotNull OnPlayerLeaveListener leave,
                             @NotNull SuperiorSkyblockInitializeListener superiorSkyblockInitialize,
                             @NotNull IslandChangeRolePrivilegeListener islandRolePrivilege,
                             @NotNull IslandDisableFlagListener islandDisableFlag) {
        this(List.of(
                toggleSneak,
                toggleFly,
                death,
                inventoryClick,
                damaged,
                join,
                leave,
                superiorSkyblockInitialize,
                islandRolePrivilege,
                islandDisableFlag
        ));
    }

    /** The list itself, so the registration mechanics can be exercised without a container. */
    public ListenerRegistrar(@NotNull List<BukkitListener> listeners) {
        this.listeners = List.copyOf(listeners);
    }

    /** @return the registered listeners, in the order they were registered */
    public @NotNull List<BukkitListener> registerAll(@NotNull PluginManager manager, @NotNull Plugin plugin) {
        listeners.forEach(listener -> listener.registerEvent(manager, plugin));
        return listeners;
    }

    /** Every listener this plugin installs; exposed so a test can assert against it. */
    public @NotNull List<BukkitListener> listeners() {
        return listeners;
    }

}
