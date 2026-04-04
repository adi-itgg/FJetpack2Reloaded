package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.command.executor;

import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums.FJ2RCommand;
import org.bukkit.command.CommandSender;

public interface FJCommandExecutor {

    boolean isSupported(FJ2RCommand cmd);

    void onCommand(CommandSender sender, FJ2RCommand cmd, String[] args);

}
