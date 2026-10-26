package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.architecture;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private static Plugin stubPlugin() {
        return (Plugin) Proxy.newProxyInstance(
                ListenerStructureTest.class.getClassLoader(),
                new Class<?>[]{Plugin.class},
                (proxy, method, args) -> null);
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
