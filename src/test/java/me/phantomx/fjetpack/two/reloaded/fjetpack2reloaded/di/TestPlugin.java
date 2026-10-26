package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.di;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * A plugin instance, and the little bit of Bukkit it talks to, without a running server.
 * <p>
 * {@link JavaPlugin} keeps a constructor meant for exactly this: it refuses to run under a
 * {@code PluginClassLoader} - which is what happens inside a real server - and works under the plain
 * application class loader a test uses. The constructor stores its arguments and builds a logger; it
 * never dereferences the server.
 * <p>
 * Two pieces of Bukkit still get touched while a config file is read: {@code saveResource} resolves
 * the embedded file through {@link #getResource} (overridden here to read the test class path, which
 * holds {@code src/main/resources}), and the permission mapper registers nodes on the plugin manager.
 * {@link #installServer()} gives both a stub, and {@link #restoreServer()} puts back whatever was
 * there - a test must not leave global state behind for the next one.
 */
public final class TestPlugin extends JavaPlugin {

    private TestPlugin(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder) {
        super(loader, description, dataFolder, null);
    }

    /** @param dataFolder the folder the config files are read from and written to */
    public static TestPlugin withDataFolder(Path dataFolder) throws IOException {
        Files.createDirectories(dataFolder);
        return new TestPlugin(
                new JavaPluginLoader(stubServer()),
                new PluginDescriptionFile("Test", "1.0", TestPlugin.class.getName()),
                dataFolder.toFile()
        );
    }

    /** The bundled resource, from the test class path. */
    @Override
    public InputStream getResource(String filename) {
        return TestPlugin.class.getClassLoader().getResourceAsStream(filename);
    }

    /**
     * Installs the stub server, once per JVM.
     * <p>
     * {@link Bukkit#setServer(Server)} refuses to replace a server that is already set, and there is
     * no way to unset it again - so this either installs the stub on the first call or leaves
     * whatever is there alone. Calling it from every test is safe and idempotent.
     */
    public static void installServer() {
        if (Bukkit.getServer() != null) return;
        Bukkit.setServer(stubServer());
    }

    /** Everything Bukkit is asked for while a config file is read, and nothing more. */
    private static Server stubServer() {
        return (Server) Proxy.newProxyInstance(
                TestPlugin.class.getClassLoader(),
                new Class<?>[]{Server.class},
                (proxy, method, args) -> switch (method.getName()) {
                    // JavaPluginLoader logs a warning through it as soon as it is built
                    case "getLogger" -> Logger.getLogger("TestPlugin");
                    case "getPluginManager" -> stubPluginManager();
                    case "getConsoleSender" -> stubConsole();
                    case "getName" -> "Test";
                    case "getVersion" -> "test";
                    case "getMotd" -> "";
                    case "toString" -> "Server(test)";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                });
    }

    /** Permissions are registered here while a file is read; a stub accepts every one of them. */
    private static PluginManager stubPluginManager() {
        return (PluginManager) Proxy.newProxyInstance(
                TestPlugin.class.getClassLoader(),
                new Class<?>[]{PluginManager.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getPermission" -> null;
                    case "getPlugin" -> null;
                    case "isPluginEnabled" -> false;
                    case "toString" -> "PluginManager(test)";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                });
    }

    private static ConsoleCommandSender stubConsole() {
        return (ConsoleCommandSender) Proxy.newProxyInstance(
                TestPlugin.class.getClassLoader(),
                new Class<?>[]{ConsoleCommandSender.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "sendMessage" -> null;
                    case "getName" -> "CONSOLE";
                    case "isOp" -> true;
                    case "hasPermission" -> true;
                    case "toString" -> "ConsoleCommandSender(test)";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                });
    }

}
