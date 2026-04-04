package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.completer.impl;

import io.avaje.inject.Component;
import lombok.RequiredArgsConstructor;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.completer.FJCommandTabCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.FJConfig;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.model.Constant;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetOrGiveFJCommandTabCompleter implements FJCommandTabCompleter {

    private final FJConfig config;
    private final JavaPlugin plugin;

    @Override
    public boolean isSupported(FJ2RCommand cmd) {
        return cmd == FJ2RCommand.GET || cmd == FJ2RCommand.GIVE;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, FJ2RCommand cmd, String[] args) {
        if (args.length == 2) {
            val suggests = new ArrayList<String>();
            if (sender instanceof Player) {
                suggests.addAll(config.jetpacks().keySet());
            }
            suggests.addAll(getOnlinePlayers(plugin.getServer()));
            return copyPartialMatches(args[1], suggests);
        }
        if (args.length == 3) {
            if (config.jetpacks().containsKey(args[1])) {
                return copyPartialMatches(args[2], Constant.AMOUNTS);
            } else {
                return copyPartialMatches(args[2], config.jetpacks().keySet());
            }
        }
        if (args.length == 4 && config.jetpacks().containsKey(args[2])) {
            return copyPartialMatches(args[3], Constant.AMOUNTS);
        }
        return null;
    }

}
