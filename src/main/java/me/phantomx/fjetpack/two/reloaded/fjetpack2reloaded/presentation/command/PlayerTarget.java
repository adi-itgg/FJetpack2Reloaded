package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command;

import jakarta.inject.Singleton;
import lombok.val;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.application.Log;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.infrastructure.config.YamlConfigRepository;
import me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.message.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

/** Resolves which player a command targets, with the player-only message in one place. */
@Singleton
public class PlayerTarget {

    private final Messages messages;
    private final YamlConfigRepository configs;
    private final Log log;

    public PlayerTarget(@NotNull Messages messages, @NotNull YamlConfigRepository configs) {
        this.messages = messages;
        this.configs = configs;
        this.log = new Log(getClass(), messages);
    }

    /** The sender as a player, or null after telling it that the command is player only. */
    public @Nullable Player asPlayer(@NotNull CommandSender sender) {
        if (sender instanceof Player player) {
            log.debug("target: sender=%s is player", player.getName());
            return player;
        }
        log.debug("target: sender=%s is not a player", sender.getName());
        messages.sendMessage(configs.message().getCmdPlayerOnly());
        return null;
    }

    /** First online player named in the arguments, falling back to the sender when it is a player. */
    public @Nullable Player of(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        val target = Arrays.stream(args)
                .map(Bukkit::getPlayerExact)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> asPlayer(sender));
        log.debug("target: sender=%s args=%s -> target=%s", sender.getName(), Arrays.toString(args),
                target == null ? "null" : target.getName());
        return target;
    }

}
