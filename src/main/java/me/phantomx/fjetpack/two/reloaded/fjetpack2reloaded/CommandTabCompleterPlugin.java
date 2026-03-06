package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded;

import io.avaje.inject.PostConstruct;
import io.avaje.inject.Prototype;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.Configs;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.FJConfig;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.AccessDeniedLevelException;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Messages;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.Placeholder;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc.CommandExtensionPlugin;
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
@SuperBuilder
public class CommandTabCompleterPlugin extends CommandExtensionPlugin {

    private final List<String> amounts = List.of("32", "64", "96", "128", "256");
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

        switch (cmd) {
            case SET -> { // cmd set completions
                if (args.length == 2) {
                    return copyPartialMatches(args[1], config.jetpacks().keySet());
                }
                if (args.length == 3) {
                    return copyPartialMatches(args[2], amounts);
                }
            }
            case SET_FUEL -> { // cmd set fuel completions
                if (args.length == 2) {
                    return copyPartialMatches(args[1], amounts);
                }
            }
            case GET, GIVE -> { // cmd get/give jetpack completions
                if (args.length == 2) {
                    val suggests = new ArrayList<String>();
                    if (sender instanceof Player) {
                        suggests.addAll(config.jetpacks().keySet());
                    }
                    suggests.addAll(getOnlinePlayers());
                    return copyPartialMatches(args[1], suggests);
                }
                if (args.length == 3) {
                    if (config.jetpacks().containsKey(args[1])) {
                        return copyPartialMatches(args[2], amounts);
                    } else {
                        return copyPartialMatches(args[2], config.jetpacks().keySet());
                    }
                }
                if (args.length == 4 && config.jetpacks().containsKey(args[2])) {
                    return copyPartialMatches(args[3], amounts);
                }
            }
            case GET_FUEL, GIVE_FUEL -> {
                if (args.length == 2) {
                    val suggests = new ArrayList<String>();
                    if (sender instanceof Player) {
                        suggests.addAll(config.customFuels().keySet());
                    }
                    suggests.addAll(getOnlinePlayers());
                    return copyPartialMatches(args[1], suggests);
                }
                if (args.length == 3) {
                    if (config.customFuels().containsKey(args[1])) {
                        return copyPartialMatches(args[2], amounts);
                    } else {
                        return copyPartialMatches(args[2], config.customFuels().keySet());
                    }
                }
                if (args.length == 4 && config.customFuels().containsKey(args[2])) {
                    return copyPartialMatches(args[3], amounts);
                }
            }
        }

        return Collections.emptyList();
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

                // TODO set item as jetpack

                return true;
            }
            default -> throw new IllegalStateException("Unexpected value: " + cmd);
        }


        return true;
    }
}
