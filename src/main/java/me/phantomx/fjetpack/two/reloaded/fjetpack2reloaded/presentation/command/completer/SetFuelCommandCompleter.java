package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.completer;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/** {@code /fj2r setfuel <amount>} */
public class SetFuelCommandCompleter implements CommandCompleter {

    @Override
    public @NotNull Set<FJ2RCommand> commands() {
        return Set.of(FJ2RCommand.SET_FUEL);
    }

    @Override
    public @NotNull List<String> complete(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length == 2)
            return Completions.partial(args[1], Completions.AMOUNTS);
        return List.of();
    }

}
