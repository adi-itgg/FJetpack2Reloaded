package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.completer;

import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** {@code /fj2r getfuel|givefuel [player] [customFuel] [amount]} */
public class GetOrGiveFuelCommandCompleter implements CommandCompleter {

    private final YamlConfigRepository configs;

    public GetOrGiveFuelCommandCompleter(@NotNull YamlConfigRepository configs) {
        this.configs = configs;
    }

    @Override
    public @NotNull Set<FJ2RCommand> commands() {
        return Set.of(FJ2RCommand.GET_FUEL, FJ2RCommand.GIVE_FUEL);
    }

    @Override
    public @NotNull List<String> complete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length == 2) {
            val suggests = new ArrayList<String>();
            if (sender instanceof Player)
                suggests.addAll(configs.customFuels().keySet());
            suggests.addAll(Completions.onlinePlayers());
            return Completions.partial(args[1], suggests);
        }
        if (args.length == 3) {
            if (configs.customFuels().containsKey(args[1]))
                return Completions.partial(args[2], Completions.AMOUNTS);
            return Completions.partial(args[2], configs.customFuels().keySet());
        }
        if (args.length == 4 && configs.customFuels().containsKey(args[2]))
            return Completions.partial(args[3], Completions.AMOUNTS);
        return List.of();
    }

}
