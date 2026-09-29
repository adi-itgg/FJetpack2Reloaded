package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.di;

import io.avaje.inject.Bean;
import io.avaje.inject.External;
import io.avaje.inject.Factory;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
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
 *   <li>{@link MessageSink} - {@link Messages} is both the message service and the output port;
 *       the port is published so infrastructure can depend on it instead of the class.</li>
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

    /** Subcommands without further arguments; its varargs constructor is not injectable. */
    @Bean
    CommandCompleter noArgsCompleter() {
        return new NoArgsCommandCompleter(FJ2RCommand.HELP, FJ2RCommand.RELOAD, FJ2RCommand.CHECK_UPDATE);
    }

}
