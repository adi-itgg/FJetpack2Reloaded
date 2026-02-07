package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded;

import io.avaje.inject.Prototype;
import lombok.RequiredArgsConstructor;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.Configs;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.FJConfig;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Permissions;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Prototype
@RequiredArgsConstructor
public class TabCompleter {

    private final List<String> amounts = List.of("32", "64", "96", "128", "256");

    private final FJConfig config;
    private final Server server;

    private List<String> copyPartialMatches(String token, Iterable<String> suggest) {
        return StringUtil.copyPartialMatches(token, suggest, new ArrayList<>());
    }

    private List<String> getOnlinePlayers() {
        val onlinePlayers = new ArrayList<String>();
        for (Player player : server.getOnlinePlayers()) {
            onlinePlayers.add(player.getName());
            onlinePlayers.add(player.getDisplayName());
        }
        return onlinePlayers.stream().distinct().toList();
    }

    public List<String> onTab(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return copyPartialMatches(args[0], FJ2RCommand.COMMANDS.stream().filter(cmd -> Permissions.hasPermission(sender, cmd)).toList());
        }

        if (!Permissions.hasPermission(sender, args[0])) {
            return Collections.emptyList();
        }

        // cmd set completions
        if (FJ2RCommand.SET.isEqual(args[0])) {
            if (args.length == 2) {
                return copyPartialMatches(args[1], config.jetpacks().keySet());
            }
            if (args.length == 3) {
                return copyPartialMatches(args[2], amounts);
            }
        }

        // cmd set fuel completions
        if (FJ2RCommand.SET_FUEL.isEqual(args[0]) && args.length == 2) {
            return copyPartialMatches(args[1], amounts);
        }

        // cmd get/give jetpack completions
        if (FJ2RCommand.GET.isEqual(args[0]) || FJ2RCommand.GIVE.isEqual(args[0])) {
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

        // TODO get/give fuel


        return Collections.emptyList();
    }

}
