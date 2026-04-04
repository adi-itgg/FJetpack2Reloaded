package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.executor.impl;

import io.avaje.inject.Component;
import io.avaje.inject.Primary;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.executor.FJCommandExecutor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@Primary
@Component
public class CompositeFJCommandExecutor implements FJCommandExecutor {

    private final FJCommandExecutor[] executors;

    public CompositeFJCommandExecutor(List<FJCommandExecutor> executors) {
        this.executors = executors.stream().filter(e -> !(e instanceof CompositeFJCommandExecutor)).toArray(FJCommandExecutor[]::new);
    }

    @Override
    public boolean isSupported(FJ2RCommand cmd) {
        return true;
    }

    @Override
    public void onCommand(CommandSender sender, FJ2RCommand cmd, String[] args) {
        for (FJCommandExecutor executor : executors) {
            if (executor.isSupported(cmd)) {
                executor.onCommand(sender, cmd, args);
                return;
            }
        }
    }

}
