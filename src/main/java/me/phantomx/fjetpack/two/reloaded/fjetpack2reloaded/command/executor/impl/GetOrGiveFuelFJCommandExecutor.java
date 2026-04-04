package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.executor.impl;

import io.avaje.inject.Component;
import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.executor.FJCommandExecutor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import org.bukkit.command.CommandSender;

@Component
@RequiredArgsConstructor
public class GetOrGiveFuelFJCommandExecutor implements FJCommandExecutor {

    @Override
    public boolean isSupported(FJ2RCommand cmd) {
        return cmd == FJ2RCommand.GET_FUEL || cmd == FJ2RCommand.GIVE_FUEL;
    }

    @Override
    public void onCommand(CommandSender sender, FJ2RCommand cmd, String[] args) {
        // TODO implement command GET_FUEL or GIVE_FUEL
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
