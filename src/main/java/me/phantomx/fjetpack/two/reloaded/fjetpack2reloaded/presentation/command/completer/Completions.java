package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.completer;

import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/** Shared suggestion helpers. */
public final class Completions {

    public static final List<String> AMOUNTS = List.of("32", "64", "96", "128", "256");

    private Completions() {
    }

    public static @NotNull List<String> partial(String token, Iterable<String> suggest) {
        return StringUtil.copyPartialMatches(token, suggest, new ArrayList<>());
    }

    static @NotNull List<String> onlinePlayers() {
        val onlinePlayers = new ArrayList<String>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            onlinePlayers.add(player.getName());
            onlinePlayers.add(player.getDisplayName());
        }
        return onlinePlayers.stream().distinct().toList();
    }

}
