package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.handler;

import jakarta.inject.Singleton;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.port.MessageSink;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command.FJ2RCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/** {@code /fj2r reload} */
@Singleton
public class ReloadCommandHandler implements CommandHandler {

    private final YamlConfigRepository configs;
    private final MessageSink messages;

    public ReloadCommandHandler(@NotNull YamlConfigRepository configs, @NotNull MessageSink messages) {
        this.configs = configs;
        this.messages = messages;
    }

    @Override
    public @NotNull Set<FJ2RCommand> commands() {
        return Set.of(FJ2RCommand.RELOAD);
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        requirePermission(sender, FJ2RCommand.RELOAD);
        configs.reload(sender);

        // the load itself already reported each file; this is the part a sender cannot see: whether
        // what is now loaded can be used at all
        if (configs.jetpacks().isEmpty())
            messages.send(sender, "&cReload finished, but no jetpack is loaded - players have nothing to fly with");
    }

}
