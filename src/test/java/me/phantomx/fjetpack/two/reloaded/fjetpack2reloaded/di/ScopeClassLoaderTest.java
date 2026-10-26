package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.di;

import io.avaje.inject.spi.AvajeModule;
import io.avaje.inject.spi.InjectExtension;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.message.Messages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ServiceLoader;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Avaje finds the generated module through {@code META-INF/services}, using the class loader it is
 * given and falling back to the thread context loader. On a server that context loader belongs to the
 * server and cannot see the plugin's jar, so the lookup finds nothing and the first
 * {@code BeanScope.get} throws "No bean found" - which is what happened on Paper.
 * <p>
 * {@code onEnable} now passes {@code getClass().getClassLoader()} explicitly. These assertions cover
 * the half that can be checked without a server: that this loader - the one the plugin would pass -
 * does find the module, and that the module declares the bean the entry point asks for first.
 * <p>
 * The failure mode itself cannot be reproduced faithfully here: a child loader in this JVM still
 * delegates to the application loader, which has the module on its class path. It was reproduced
 * outside the test suite, against the built jar.
 */
@DisplayName("Bean scope class loader")
class ScopeClassLoaderTest {

    @Test
    @DisplayName("the plugin's own loader finds the module the build generated")
    void thePluginClassLoaderFindsTheModule() {
        assertEquals(1, moduleCount(getClass().getClassLoader()),
                "the generated module must be discoverable from the loader the plugin passes");
    }

    @Test
    @DisplayName("that module declares the bean the entry point asks for first")
    void theFoundModuleCarriesTheBeansThePluginNeeds() {
        var module = ServiceLoader.load(InjectExtension.class, getClass().getClassLoader()).stream()
                .map(ServiceLoader.Provider::get)
                .filter(AvajeModule.class::isInstance)
                .map(AvajeModule.class::cast)
                .filter(candidate -> candidate.getClass().getName().contains("Fjetpack2reloadedModule"))
                .findFirst()
                .orElseThrow();

        Set<Class<?>> provided = Set.of(module.classes());
        assertTrue(provided.contains(Messages.class), "the bean onEnable failed on");
    }

    private static long moduleCount(ClassLoader loader) {
        return ServiceLoader.load(InjectExtension.class, loader).stream()
                .map(ServiceLoader.Provider::get)
                .filter(AvajeModule.class::isInstance)
                .filter(module -> module.getClass().getName().contains("Fjetpack2reloadedModule"))
                .count();
    }

}
