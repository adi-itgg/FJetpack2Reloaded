package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded;

import io.avaje.inject.PostConstruct;
import io.avaje.inject.Prototype;
import lombok.RequiredArgsConstructor;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.completer.FJCommandTabCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.FJConfig;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.AccessDeniedLevelException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.JetpackItemFactory;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Placeholder;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc.FJVersion;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Permissions;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Prototype
@RequiredArgsConstructor
public class CommandTabCompleterPlugin {

    private final Logger log;
    private final JavaPlugin plugin;
    private final FJConfig config;
    private final FJVersion version;
    private final JetpackItemFactory jetpackItemFactory;
    private final FJCommandTabCompleter completer;

    private List<String> helpText;

    @SuppressWarnings("DataFlowIssue")
    @PostConstruct
    void init() {
        try (val br = new BufferedReader(new InputStreamReader(plugin.getResource("help.txt")))) {
            this.helpText = br.lines()
                    .map(s -> s.replace(Placeholder.VERSION, plugin.getDescription().getVersion()))
                    .toList();
        } catch (Exception e) {
            log.error("Failed to load help.txt", e);
        }
    }

    public @Nullable List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, @NonNull String[] args) {
        if (args.length == 1) {
            return copyPartialMatches(args[0], FJ2RCommand.COMMANDS.stream().filter(cmd -> Permissions.hasPermission(sender, cmd)).toList());
        }

        val cmd = FJ2RCommand.parse(args[0]);

        // unknown command
        if (cmd == null || !Permissions.hasPermission(sender, cmd)) {
            return Collections.emptyList();
        }

        return completer.onTabComplete(sender, cmd, args);
    }


    @SuppressWarnings("deprecation")
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (args.length == 0) {
            return true;
        }

        val cmd = FJ2RCommand.parse(args[0]);

        // unknown command
        if (cmd == null) {
            Messages.sendMessage(false, sender, String.join("", helpText));
            return true;
        }

        if (!Permissions.hasPermission(sender, cmd)) {
            throw new AccessDeniedLevelException(cmd.cmd());
        }

        switch (cmd) {
            case HELP -> Messages.sendMessage(false, sender, String.join("", helpText));
            case RELOAD -> config.reloadConfig(sender);
            case CHECK_UPDATE -> version.checkUpdate(sender);
            case SET -> {
                if (args.length == 1) {
                    Messages.sendMessage(sender, this.helpText.get(6));
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    Messages.sendMessage(config.message().getCmdPlayerOnly());
                    return true;
                }

                val item = version.getServerVersion() > 8 ? player.getInventory().getItemInMainHand() : player.getItemInHand();

                if (item.getItemMeta() == null || item.getType() == Material.AIR) {
                    Messages.sendMessage(config.message().getNoItemInMainHand());
                    return true;
                }

                val jetpackItem = jetpackItemFactory.createJetpackItem(sender, args[1], args.length == 3 ? Long.parseLong(args[2]) : 0);
                if (version.getServerVersion() > 11) {
                    player.getInventory().setItemInMainHand(jetpackItem);
                } else {
                    player.setItemInHand(jetpackItem);
                }

                Messages.sendMessage(sender, config.message().getCmdSet()
                        .replace(Placeholder.JETPACK, args[1])
                        .replace(Placeholder.FUEL_VALUE, String.valueOf(args.length == 3 ? args[2] : 0))
                );
                return true;
            }
            case GET, GIVE -> {
                // TODO implement
            }
            default -> throw new IllegalStateException("Unexpected value: " + cmd);
        }


        return true;
    }


    private List<String> copyPartialMatches(String token, Iterable<String> suggest) {
        return StringUtil.copyPartialMatches(token, suggest, new ArrayList<>());
    }

    private List<String> getOnlinePlayers(Server server) {
        return server.getOnlinePlayers().stream()
                .map(Player::getName)
                .distinct()
                .toList();
    }
}
