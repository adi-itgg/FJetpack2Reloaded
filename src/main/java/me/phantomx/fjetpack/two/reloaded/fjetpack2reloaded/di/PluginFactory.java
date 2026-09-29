package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.di;

import io.avaje.inject.Bean;
import io.avaje.inject.External;
import io.avaje.inject.Factory;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ClaimLookup;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.IslandLookup;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.ItemTags;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.mapping.ConfigValueMappers;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook.GriefPreventionClaims;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook.SuperiorSkyblockIslands;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.item.ItemTagCodec;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.completer.CommandCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.completer.NoArgsCommandCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.message.Messages;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Beans the generator cannot derive from a class alone.
 * <ul>
 *   <li>{@link PluginFactory.Initiator} - the plugin instance is created by Bukkit, so it is handed
 *       to the container at runtime and published here as the {@link JavaPlugin} bean.</li>
 *   <li>the output and lookup ports - the adapter implementing each port is a concrete bean, but the
 *       use cases depend on the interface, so the port is published here as well.</li>
 * </ul>
 */
@Factory
public class PluginFactory {

    /** Handed over by the plugin when it builds the scope. */
    @RequiredArgsConstructor
    public static class Initiator {
        private final @Getter JavaPlugin plugin;
    }

    private final Initiator initiator;

    @Inject
    PluginFactory(@External Initiator initiator) {
        this.initiator = initiator;
    }

    @Bean
    JavaPlugin plugin() {
        return initiator.getPlugin();
    }

    @Bean
    MessageSink messageSink(Messages messages) {
        return messages;
    }

    @Bean
    ConfigRepository configRepository(YamlConfigRepository repository) {
        return repository;
    }

    @Bean
    ItemTags itemTags(ItemTagCodec codec) {
        return codec;
    }

    @Bean
    ClaimLookup claimLookup(GriefPreventionClaims claims) {
        return claims;
    }

    @Bean
    IslandLookup islandLookup(SuperiorSkyblockIslands islands) {
        return islands;
    }

    /** Built from a static factory, so the generator cannot derive it. */
    @Bean
    ConfigValueMappers configValueMappers() {
        return ConfigValueMappers.defaults();
    }

    /** Subcommands without further arguments; its varargs constructor is not injectable. */
    @Bean
    CommandCompleter noArgsCompleter() {
        return new NoArgsCommandCompleter(FJ2RCommand.HELP, FJ2RCommand.RELOAD, FJ2RCommand.CHECK_UPDATE);
    }

}
