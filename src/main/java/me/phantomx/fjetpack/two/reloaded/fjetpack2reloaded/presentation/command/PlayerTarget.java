package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command;

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
public class PlayerTarget {

    private final Messages messages;
    private final YamlConfigRepository configs;

    public PlayerTarget(@NotNull Messages messages, @NotNull YamlConfigRepository configs) {
        this.messages = messages;
        this.configs = configs;
    }

    /** The sender as a player, or null after telling it that the command is player only. */
    public @Nullable Player asPlayer(@NotNull CommandSender sender) {
        if (sender instanceof Player player) return player;
        messages.sendMessage(configs.message().getCmdPlayerOnly());
        return null;
    }

    /** First online player named in the arguments, falling back to the sender when it is a player. */
    public @Nullable Player of(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return Arrays.stream(args)
                .map(Bukkit::getPlayerExact)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> asPlayer(sender));
    }

}
