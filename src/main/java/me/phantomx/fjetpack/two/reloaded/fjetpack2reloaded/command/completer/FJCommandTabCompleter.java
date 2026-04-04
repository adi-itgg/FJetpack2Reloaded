package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.completer;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public interface FJCommandTabCompleter {

    boolean isSupported(FJ2RCommand cmd);

    List<String> onTabComplete(CommandSender sender, FJ2RCommand cmd, String[] args);



    default  List<String> copyPartialMatches(String token, Iterable<String> suggest) {
        return StringUtil.copyPartialMatches(token, suggest, new ArrayList<>());
    }

    default List<String> getOnlinePlayers(Server server) {
        return server.getOnlinePlayers().stream()
                .map(Player::getName)
                .distinct()
                .toList();
    }

}
