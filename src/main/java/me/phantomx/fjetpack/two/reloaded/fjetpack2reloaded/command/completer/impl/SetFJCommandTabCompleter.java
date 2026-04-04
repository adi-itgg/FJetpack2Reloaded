package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.completer.impl;

import io.avaje.inject.Component;
import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.completer.FJCommandTabCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.FJConfig;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.model.Constant;
import org.bukkit.command.CommandSender;

import java.util.List;


@Component
@RequiredArgsConstructor
public class SetFJCommandTabCompleter implements FJCommandTabCompleter {

    private final FJConfig config;

    @Override
    public boolean isSupported(FJ2RCommand cmd) {
        return cmd == FJ2RCommand.SET;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, FJ2RCommand cmd, String[] args) {
        if (args.length == 2) {
            return copyPartialMatches(args[1], config.jetpacks().keySet());
        }
        if (args.length == 3) {
            return copyPartialMatches(args[2], Constant.AMOUNTS);
        }
        return null;
    }

}
