package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.completer;

import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/** {@code /fj2r set <jetpack> <fuel>} */
@Singleton
public class SetCommandCompleter implements CommandCompleter {

    private final YamlConfigRepository configs;

    public SetCommandCompleter(@NotNull YamlConfigRepository configs) {
        this.configs = configs;
    }

    @Override
    public @NotNull Set<FJ2RCommand> commands() {
        return Set.of(FJ2RCommand.SET);
    }

    @Override
    public @NotNull List<String> complete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length == 2)
            return Completions.partial(args[1], configs.jetpacks().keySet());
        if (args.length == 3)
            return Completions.partial(args[2], Completions.AMOUNTS);
        return List.of();
    }

}
