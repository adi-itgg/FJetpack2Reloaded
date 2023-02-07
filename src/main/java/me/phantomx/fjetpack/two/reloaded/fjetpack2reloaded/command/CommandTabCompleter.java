package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command;

import lombok.Getter;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.Configs;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.logging.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.util.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommandTabCompleter {

    @Getter(lazy = true)
    private static final List<String> commands = Arrays.stream(FJ2RCommand.values()).map(cmd -> cmd.name().replace("_", "").toLowerCase()).toList();
    @Getter(lazy = true)
    private static final List<String> amounts = List.of("32", "64", "96", "128", "256");

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


    public static @NotNull List<String> onTab(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        Log.log("onTab - sender=%s, command=%s, alias=%s, args=%s, argsLength=%s", sender.getName(), command.getName(), alias, args, args.length);
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
            return copyPartialMatches(args[1], Configs.getJetpacksLoaded().keySet());
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
                    suggests.addAll(Configs.getJetpacksLoaded().keySet());
                suggests.addAll(getOnlinePlayers());
                return copyPartialMatches(args[1], suggests);
            }
            if (args.length == 3) {
                if (Configs.getJetpacksLoaded().containsKey(args[1]))
                    return copyPartialMatches(args[2], getAmounts());
                else
                    return copyPartialMatches(args[2], Configs.getJetpacksLoaded().keySet());
            }
            if (args.length == 4 && Configs.getJetpacksLoaded().containsKey(args[2]))
                return copyPartialMatches(args[3], getAmounts());
        }

        // cmd get/give fuel
        if (args[0].equalsIgnoreCase(cmd(FJ2RCommand.GET_FUEL)) || args[0].equalsIgnoreCase(cmd(FJ2RCommand.GIVE_FUEL))) {
            if (args.length == 2) {
                val suggests = new ArrayList<String>();
                if (sender instanceof Player)
                    suggests.addAll(Configs.getCustomFuelLoaded().keySet());
                suggests.addAll(getOnlinePlayers());
                return copyPartialMatches(args[1], suggests);
            }
            if (args.length == 3) {
                if (Configs.getCustomFuelLoaded().containsKey(args[1]))
                    return copyPartialMatches(args[2], getAmounts());
                else
                    return copyPartialMatches(args[2], Configs.getCustomFuelLoaded().keySet());
            }
            if (args.length == 4 && Configs.getCustomFuelLoaded().containsKey(args[2]))
                return copyPartialMatches(args[3], getAmounts());
        }


        return Collections.emptyList();
    }

}
