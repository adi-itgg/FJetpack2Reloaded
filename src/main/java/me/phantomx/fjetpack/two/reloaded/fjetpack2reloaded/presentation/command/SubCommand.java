package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/** Contract shared by everything that is bound to one or more subcommands. */
public interface SubCommand {

    /** Subcommands this piece answers for. */
    @NotNull Set<FJ2RCommand> commands();

    default boolean supports(@NotNull String command) {
        for (FJ2RCommand candidate : commands())
            if (candidate.literal().equalsIgnoreCase(command)) return true;
        return false;
    }

}
