package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.completer.impl;

import io.avaje.inject.Component;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.completer.FJCommandTabCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.model.Constant;
import org.bukkit.command.CommandSender;

import java.util.List;


@Component
public class SetFuelFJCommandTabCompleter implements FJCommandTabCompleter {

    @Override
    public boolean isSupported(FJ2RCommand cmd) {
        return cmd == FJ2RCommand.SET_FUEL;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, FJ2RCommand cmd, String[] args) {
        if (args.length == 2) {
            return copyPartialMatches(args[1], Constant.AMOUNTS);
        }
        return null;
    }

}
