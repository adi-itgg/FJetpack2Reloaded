package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.executor.impl;

import io.avaje.inject.Component;
import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.executor.FJCommandExecutor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.misc.FJVersion;
import org.bukkit.command.CommandSender;

@Component
@RequiredArgsConstructor
public class CheckUpdateFJCommandExecutor implements FJCommandExecutor {

    private final FJVersion version;

    @Override
    public boolean isSupported(FJ2RCommand cmd) {
        return cmd == FJ2RCommand.CHECK_UPDATE;
    }

    @Override
    public void onCommand(CommandSender sender, FJ2RCommand cmd, String[] args) {
        version.checkUpdate(sender);
    }

}
