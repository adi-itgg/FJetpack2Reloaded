package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command;

import lombok.Getter;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.logging.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.message.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandTabCompleter {

    @Getter(lazy = true)
    private static final List<String> commands = Arrays.stream(FJ2RCommand.values()).map(cmd -> cmd.name().replace("_", "").toLowerCase()).toList();
    @Getter(lazy = true)
    private static final List<String> amounts = List.of("32", "64", "96", "128", "256");

    private final YamlConfigRepository configs;
    private final Log log;

    public CommandTabCompleter(@NotNull YamlConfigRepository configs, @NotNull MessageSink messages) {
        this.configs = configs;
        this.log = new Log(getClass(), messages);
    }

    private static @NotNull String cmd(@NotNull FJ2RCommand cmd) {
        return FJ2RCommandExecutor.cmd(cmd);
    }

    private static @NotNull List<String> copyPartialMatches(String token, Iterable<String> suggest) {
        return StringUtil.copyPartialMatches(token, suggest, new ArrayList<>());
    }

    private static @NotNull List<String> getOnlinePlayers() {
        val onlinePlayers = new ArrayList<String>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            onlinePlayers.add(player.getName());
            onlinePlayers.add(player.getDisplayName());
        }
        return onlinePlayers.stream().distinct().toList();
    }

    public @NotNull List<String> onTab(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        log.log("onTab - sender=%s, command=%s, alias=%s, args=%s, argsLength=%s", sender.getName(), command.getName(), alias, args, args.length);
        if (args.length == 1)
            return copyPartialMatches(args[0],
                    getCommands()
                            .stream()
                            .filter(cmd -> Permissions.hasPermission(sender, cmd))
                            .toList()
            );

        if (!Permissions.hasPermission(sender, args[0]))
            return Collections.emptyList();

        // cmd set completions
        if (args[0].equalsIgnoreCase(cmd(FJ2RCommand.SET)) && args.length == 2)
            return copyPartialMatches(args[1], configs.jetpacks().keySet());
        if (args[0].equalsIgnoreCase(cmd(FJ2RCommand.SET)) && args.length == 3)
            return copyPartialMatches(args[2], getAmounts());

        // cmd set fuel completions
        if (args[0].equalsIgnoreCase(cmd(FJ2RCommand.SET_FUEL)) && args.length == 2)
            return copyPartialMatches(args[1], getAmounts());

        // cmd get/give
        if (args[0].equalsIgnoreCase(cmd(FJ2RCommand.GET)) || args[0].equalsIgnoreCase(cmd(FJ2RCommand.GIVE))) {
            if (args.length == 2) {
                val suggests = new ArrayList<String>();
                if (sender instanceof Player)
                    suggests.addAll(configs.jetpacks().keySet());
                suggests.addAll(getOnlinePlayers());
                return copyPartialMatches(args[1], suggests);
            }
            if (args.length == 3) {
                if (configs.jetpacks().containsKey(args[1]))
                    return copyPartialMatches(args[2], getAmounts());
                else
                    return copyPartialMatches(args[2], configs.jetpacks().keySet());
            }
            if (args.length == 4 && configs.jetpacks().containsKey(args[2]))
                return copyPartialMatches(args[3], getAmounts());
        }

        // cmd get/give fuel
        if (args[0].equalsIgnoreCase(cmd(FJ2RCommand.GET_FUEL)) || args[0].equalsIgnoreCase(cmd(FJ2RCommand.GIVE_FUEL))) {
            if (args.length == 2) {
                val suggests = new ArrayList<String>();
                if (sender instanceof Player)
                    suggests.addAll(configs.customFuels().keySet());
                suggests.addAll(getOnlinePlayers());
                return copyPartialMatches(args[1], suggests);
            }
            if (args.length == 3) {
                if (configs.customFuels().containsKey(args[1]))
                    return copyPartialMatches(args[2], getAmounts());
                else
                    return copyPartialMatches(args[2], configs.customFuels().keySet());
            }
            if (args.length == 4 && configs.customFuels().containsKey(args[2]))
                return copyPartialMatches(args[3], getAmounts());
        }


        return Collections.emptyList();
    }

}
