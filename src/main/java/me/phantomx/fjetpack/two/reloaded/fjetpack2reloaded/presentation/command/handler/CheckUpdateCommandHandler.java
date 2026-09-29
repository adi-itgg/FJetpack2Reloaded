package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.handler;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.util.UpdateChecker;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/** {@code /fj2r checkupdate} */
public class CheckUpdateCommandHandler implements CommandHandler {

    private final UpdateChecker updateChecker;

    public CheckUpdateCommandHandler(@NotNull UpdateChecker updateChecker) {
        this.updateChecker = updateChecker;
    }

    @Override
    public @NotNull Set<FJ2RCommand> commands() {
        return Set.of(FJ2RCommand.CHECK_UPDATE);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        requirePermission(sender, FJ2RCommand.CHECK_UPDATE);
        updateChecker.check(sender);
    }

}
