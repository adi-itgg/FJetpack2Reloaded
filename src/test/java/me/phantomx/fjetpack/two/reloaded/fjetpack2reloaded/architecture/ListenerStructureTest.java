package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.architecture;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.BukkitListener;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.hook.SuperiorSkyblock;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.listener.ListenerRegistrar;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The listener structure, checked where it can be checked without a server.
 * <p>
 * Two things used to rot silently: {@code @EventHandler} methods piling up in one class, and a
 * listener that nobody registered. Both are cheap to assert over the actual classes.
 */
@DisplayName("Listener structure")
class ListenerStructureTest {

    private static final String BASE = "me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded";

    @Test
    @DisplayName("every listener class handles exactly one Bukkit event")
    void oneEventHandlerPerListenerClass() {
        var offenders = new ArrayList<String>();

        for (Class<?> type : listenerClasses()) {
            long handlers = eventHandlerMethods(type).size();
            if (handlers != 1) offenders.add(type.getSimpleName() + " has " + handlers);
        }

        assertTrue(offenders.isEmpty(), "one event, one class: " + offenders);
    }

    @Test
    @DisplayName("the registrar's bean constructor takes every listener")
    void beanConstructorTakesEveryListener() {
        var producer = Arrays.stream(ListenerRegistrar.class.getDeclaredConstructors())
                .filter(ctor -> ctor.isAnnotationPresent(jakarta.inject.Inject.class))
                .findFirst()
                .orElseThrow(() -> new AssertionError("the container needs one @Inject constructor"));

        var wired = Arrays.stream(producer.getParameterTypes()).toList();
        var expected = listenerClasses();
        var missing = expected.stream().filter(type -> !wired.contains(type)).map(Class::getSimpleName).toList();

        assertEquals(expected.size(), wired.size(), "constructor takes: " + wired);
        assertTrue(missing.isEmpty(), "never wired in: " + missing);
    }

    @Test
    @DisplayName("registering the listeners hands every one to Bukkit")
    void registerAllHandsEveryListenerToBukkit() {
        var registered = new ArrayList<Class<?>>();
        var registrar = new ListenerRegistrar(
                listenerClasses().stream().map(ListenerStructureTest::instantiate).toList());

        registrar.registerAll(recordingPluginManager(registered), stubPlugin());

        assertEquals(listenerClasses().size(), registered.size(), "registered classes: " + registered);
    }

    @Test
    @DisplayName("a listener whose optional plugin is missing is skipped and reported at INFO")
    void unavailableListenerIsSkipped() {
        var registered = new ArrayList<Class<?>>();
        var logged = new ArrayList<String>();
        var registrar = new ListenerRegistrar(List.of(new UnavailableListener()));

        registrar.registerAll(recordingPluginManager(registered), stubPlugin(logged));

        assertTrue(registered.isEmpty(), "handed to Bukkit: " + registered);
        assertEquals(1, logged.size(), "log lines: " + logged);
        assertTrue(logged.get(0).startsWith("INFO"), logged.get(0));
        assertTrue(logged.get(0).contains("UnavailableListener"), logged.get(0));
        assertTrue(logged.get(0).contains("optional plugin is missing"), logged.get(0));
    }

    @Test
    @DisplayName("the optional API is detected from the classpath")
    void optionalApiIsDetectedByClass() {
        assertTrue(SuperiorSkyblock.apiClassPresent(
                "com.bgsoftware.superiorskyblock.api.events.PluginInitializeEvent"));
        assertFalse(SuperiorSkyblock.apiClassPresent(
                "com.bgsoftware.superiorskyblock.api.NotInstalled"));
    }

    /** An optional listener: the registrar must ask before handing it to Bukkit. */
    private static final class UnavailableListener implements BukkitListener {

        @Override
        public String unavailableReason() {
            return "optional plugin is missing";
        }

    }

    private static Plugin stubPlugin() {
        return stubPlugin(new ArrayList<>());
    }

    /** A plugin whose logger records into {@code lines}, so a skipped listener's INFO can be seen. */
    private static Plugin stubPlugin(List<String> lines) {
        var logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                lines.add(record.getLevel() + " " + record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
        return (Plugin) Proxy.newProxyInstance(
                ListenerStructureTest.class.getClassLoader(),
                new Class<?>[]{Plugin.class},
                (proxy, method, args) -> method.getName().equals("getLogger") ? logger : null);
    }

    /** Listeners have no state worth setting up; an empty constructor is enough to register them. */
    private static me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.BukkitListener instantiate(Class<?> type) {
        try {
            return (me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.BukkitListener)
                    type.getDeclaredConstructors()[0].newInstance(nullArguments(type.getDeclaredConstructors()[0]));
        } catch (ReflectiveOperationException error) {
            throw new AssertionError("cannot instantiate " + type.getSimpleName(), error);
        }
    }

    private static Object[] nullArguments(java.lang.reflect.Constructor<?> constructor) {
        return new Object[constructor.getParameterCount()];
    }

    private static PluginManager recordingPluginManager(List<Class<?>> registered) {
        return (PluginManager) Proxy.newProxyInstance(
                ListenerStructureTest.class.getClassLoader(),
                new Class<?>[]{PluginManager.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("registerEvents")) registered.add(args[0].getClass());
                    return null;
                });
    }

    /** Every class that carries an {@code @EventHandler}, found on the classes we know are listeners. */
    private static List<Class<?>> listenerClasses() {
        return List.of(
                load("presentation.listener.OnPlayerToggleSneakListener"),
                load("presentation.listener.OnPlayerToggleFlyListener"),
                load("presentation.listener.OnPlayerDeathListener"),
                load("presentation.listener.OnPlayerInventoryClickListener"),
                load("presentation.listener.OnPlayerDamagedListener"),
                load("presentation.listener.OnPlayerJoinListener"),
                load("presentation.listener.OnPlayerLeaveListener"),
                load("infrastructure.hook.SuperiorSkyblockInitializeListener"),
                load("infrastructure.hook.IslandChangeRolePrivilegeListener"),
                load("infrastructure.hook.IslandDisableFlagListener")
        );
    }

    private static List<Method> eventHandlerMethods(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(EventHandler.class))
                .toList();
    }

    private static Class<?> load(String relative) {
        try {
            return Class.forName(BASE + "." + relative);
        } catch (ClassNotFoundException error) {
            throw new AssertionError("listener moved or renamed: " + relative, error);
        }
    }

}
