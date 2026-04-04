package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.executor.impl;

import io.avaje.inject.Component;
import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.executor.FJCommandExecutor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.config.FJConfig;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import org.bukkit.command.CommandSender;

@Component
@RequiredArgsConstructor
public class ReloadFJCommandExecutor implements FJCommandExecutor {

    private final FJConfig config;

    @Override
    public boolean isSupported(FJ2RCommand cmd) {
        return cmd == FJ2RCommand.RELOAD;
    }

    @Override
    public void onCommand(CommandSender sender, FJ2RCommand cmd, String[] args) {
        config.reloadConfig(sender);
    }

}
