package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util;

import lombok.Cleanup;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler.Catcher;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Scanner;

/** Checks the SpigotMC resource version in the background. */
public class UpdateChecker {

    private static final int RESOURCE_ID = 107883;

    private final JavaPlugin plugin;
    private final Messages messages;

    public UpdateChecker(@NotNull JavaPlugin plugin, @NotNull Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    public void check(@NotNull CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Catcher.createVoid(() -> {
                @Cleanup val inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID).openStream();
                @Cleanup val scanner = new Scanner(inputStream);
                while (scanner.hasNext()) {
                    val spigotVersion = scanner.next();
                    val pluginVersion = Integer.parseInt(
                            plugin.getDescription()
                                    .getVersion()
                                    .replaceAll("\\D+", "")
                    );

                    if (pluginVersion < Integer.parseInt(spigotVersion.replaceAll("\\D+", ""))) {
                        messages.sendMessage(sender, "&bThere is a new update available! v%s\nhttps://www.spigotmc.org/resources/fjetpack2reloaded.107883/", spigotVersion);
                        return;
                    }
                    messages.sendMessage(sender, "&aThere is not a new update available. You are using the latest version");
                }
            }).onFailure(error -> messages.sendMessage(sender, "Unable to check for updates: %s", error));
        });
    }

}
