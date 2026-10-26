package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.flight;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ProtectionGuard;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ServerVersion;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ItemTags;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * The singletons one flight is built from, handed over as a group.
 * <p>
 * Exists so {@code PlayerRegistry} and {@link FlightSession} do not grow a constructor parameter for
 * every collaborator a use case happens to want: a new dependency is added here, once.
 */
public record FlightComposition(@NotNull JavaPlugin plugin,
                                @NotNull ConfigRepository configs,
                                @NotNull MessageSink messages,
                                @NotNull ItemTags itemTags,
                                @NotNull ProtectionGuard protection,
                                @NotNull ItemFactory items,
                                @NotNull ServerVersion version) {
}
