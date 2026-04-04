package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.executor.impl;

import io.avaje.inject.Component;
import lombok.RequiredArgsConstructor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.executor.FJCommandExecutor;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.item.JetpackItemFactory;
import org.bukkit.command.CommandSender;

@Component
@RequiredArgsConstructor
public class GetOrGiveFJCommandExecutor implements FJCommandExecutor {

    private final JetpackItemFactory jetpackItemFactory;

    @Override
    public boolean isSupported(FJ2RCommand cmd) {
        return cmd == FJ2RCommand.GET || cmd == FJ2RCommand.GIVE;
    }

    @Override
    public void onCommand(CommandSender sender, FJ2RCommand cmd, String[] args) {
        // TODO implement command GET and GIVE
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
