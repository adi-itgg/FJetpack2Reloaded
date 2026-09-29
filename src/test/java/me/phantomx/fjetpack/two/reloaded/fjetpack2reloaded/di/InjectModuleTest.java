package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.di;

import io.avaje.inject.spi.AvajeModule;
import io.avaje.inject.spi.InjectExtension;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.PlayerRegistry;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.ProtectionGuard;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.CommandTabCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommandExecutor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener.RegisterEvent;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.message.Messages;
import org.junit.jupiter.api.Test;

import java.util.ServiceLoader;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The avaje module is found through {@code META-INF/services} at runtime - the same discovery that a
 * minified release build can break. No bean is constructed here, so no server is needed.
 */
class InjectModuleTest {

    @Test
    void theGeneratedModuleIsRegisteredWithTheExpectedBeans() {
        var modules = ServiceLoader.load(InjectExtension.class).stream()
                .map(ServiceLoader.Provider::get)
                .filter(AvajeModule.class::isInstance)
                .map(AvajeModule.class::cast)
                .filter(module -> module.getClass().getName().contains("Fjetpack2reloadedModule"))
                .toList();

        assertEquals(1, modules.size(), "the avaje module must be registered in META-INF/services");

        Set<Class<?>> provided = Set.of(modules.get(0).classes());
        assertTrue(provided.contains(Messages.class), "message service");
        assertTrue(provided.contains(YamlConfigRepository.class), "config repository");
        assertTrue(provided.contains(ItemFactory.class), "item factory");
        assertTrue(provided.contains(ProtectionGuard.class), "protection guard");
        assertTrue(provided.contains(PlayerRegistry.class), "player registry");
        assertTrue(provided.contains(FJ2RCommandExecutor.class), "command dispatcher");
        assertTrue(provided.contains(CommandTabCompleter.class), "completion dispatcher");
        assertTrue(provided.contains(RegisterEvent.class), "event entry point");
    }

}
