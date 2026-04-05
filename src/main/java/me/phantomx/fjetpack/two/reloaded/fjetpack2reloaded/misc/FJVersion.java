package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc;

import io.avaje.inject.Component;
import io.avaje.inject.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.net.URL;
import java.util.Scanner;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class FJVersion {

    private static final int resourceId = 107883;

    private final Logger log;
    private final Server server;
    private final JavaPlugin plugin;

    private @Getter int serverVersion;

    @PostConstruct
    void init() {
        this.serverVersion = serverVersion();
    }

    private int serverVersion() {
        val pattern = Pattern.compile("(?<= )([\\d.]+)(?=\\))"); // git-Paper-448 (MC: 1.19.3)
        val matcher = pattern.matcher(server.getVersion());
        if (matcher.find()) {
            val version = NumberUtils.toInt(matcher.group(1).split("\\.")[1], Integer.MIN_VALUE);
            if (version != Integer.MIN_VALUE) {
                return version;
            }
        }
        val versionString = server.getVersion().split("-")[0];
        if (versionString.contains(".")) {
            val version = NumberUtils.toInt(versionString.split("\\.")[1], Integer.MIN_VALUE);
            if (version != Integer.MIN_VALUE) {
                return version;
            }
        }
        return NumberUtils.toInt(versionString, 0);
    }

    public boolean isServerSupport() {
        if (serverVersion == 0) {
            log.info("&cUnknown Server Version! - " + server.getVersion());
            return false;
        }
        return true;
    }

    public void checkUpdate(CommandSender sender) {
        server.getScheduler().runTaskAsynchronously(plugin, () -> {
            var url = "https://api.spigotmc.org/legacy/update.php?resource=" + resourceId;
            try (var scanner = new Scanner(new URL(url).openStream())) {
                if (!scanner.hasNext()) {
                    // cannot check for updates
                    Messages.sendMessage(sender, "&cUnable to check for updates");
                    return;
                }
                var spigotVersion = NumberUtils.toInt(scanner.next().replaceAll("\\D+", ""), 0);
                var pluginVersion = NumberUtils.toInt(plugin.getDescription().getVersion().replaceAll("\\D+", ""), 0);
                var hasUpdate = pluginVersion < spigotVersion;

                if (hasUpdate) {
                    Messages.sendMessage(sender, "&aYou are using an outdated version of FJetpack2Reloaded");
                    return;
                }
                Messages.sendMessage(sender, "&aYou are using the latest version of FJetpack2Reloaded");
            } catch (Exception e) {
                log.error("Unable to check for updates: {}", e.getMessage());
            }
        });
    }

}
