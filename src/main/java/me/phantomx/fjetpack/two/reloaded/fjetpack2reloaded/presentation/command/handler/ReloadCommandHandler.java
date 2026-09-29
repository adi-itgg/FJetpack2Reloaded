package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.handler;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/** {@code /fj2r reload} */
public class ReloadCommandHandler implements CommandHandler {

    private final YamlConfigRepository configs;

    public ReloadCommandHandler(@NotNull YamlConfigRepository configs) {
        this.configs = configs;
    }

    @Override
    public @NotNull Set<FJ2RCommand> commands() {
        return Set.of(FJ2RCommand.RELOAD);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        requirePermission(sender, FJ2RCommand.RELOAD);
        configs.reload(sender);
    }

}
