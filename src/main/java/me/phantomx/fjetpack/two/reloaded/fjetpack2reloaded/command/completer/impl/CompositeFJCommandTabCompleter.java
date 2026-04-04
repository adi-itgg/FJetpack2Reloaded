package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.completer.impl;

import io.avaje.inject.Component;
import io.avaje.inject.Primary;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.completer.FJCommandTabCompleter;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Primary
@Component
public class CompositeFJCommandTabCompleter implements FJCommandTabCompleter {

    private final FJCommandTabCompleter[] completers;

    public CompositeFJCommandTabCompleter(List<FJCommandTabCompleter> completers) {
        this.completers = completers.stream().filter(c -> !(c instanceof CompositeFJCommandTabCompleter)).toArray(FJCommandTabCompleter[]::new);
    }

    @Override
    public boolean isSupported(FJ2RCommand cmd) {
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, FJ2RCommand cmd, String[] args) {
        var result = new ArrayList<String>();
        for (FJCommandTabCompleter completer : completers) {
            if (completer.isSupported(cmd)) {
                result.addAll(Objects.requireNonNullElse(completer.onTabComplete(sender, cmd, args), Collections.emptyList()));
                break;
            }
        }
        return result;
    }

}
