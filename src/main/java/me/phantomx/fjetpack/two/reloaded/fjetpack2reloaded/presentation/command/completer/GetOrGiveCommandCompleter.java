package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.completer;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * {@code /fj2r get|give [player] [jetpack] [fuel]} - arguments can come in any order, so the
 * suggestions depend on which of them is already present.
 */
@Singleton
public class GetOrGiveCommandCompleter implements CommandCompleter {

    private final YamlConfigRepository configs;

    public GetOrGiveCommandCompleter(@NotNull YamlConfigRepository configs) {
        this.configs = configs;
    }

    @Override
    public @NotNull Set<FJ2RCommand> commands() {
        return Set.of(FJ2RCommand.GET, FJ2RCommand.GIVE);
    }

    @Override
    public @NotNull List<String> complete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length == 2) {
            val suggests = new ArrayList<String>();
            if (sender instanceof Player)
                suggests.addAll(configs.jetpacks().keySet());
            suggests.addAll(Completions.onlinePlayers());
            return Completions.partial(args[1], suggests);
        }
        if (args.length == 3) {
            if (configs.jetpacks().containsKey(args[1]))
                return Completions.partial(args[2], Completions.AMOUNTS);
            return Completions.partial(args[2], configs.jetpacks().keySet());
        }
        if (args.length == 4 && configs.jetpacks().containsKey(args[2]))
            return Completions.partial(args[3], Completions.AMOUNTS);
        return List.of();
    }

}
