package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.presentation.command;

import org.jetbrains.annotations.NotNull;

public enum FJ2RCommand {
    HELP,
    RELOAD,
    SET,
    GET,
    GIVE,
    SET_FUEL,
    GET_FUEL,
    GIVE_FUEL,
    CHECK_UPDATE;

    /** Command literal as typed and as used for permissions, e.g. SET_FUEL -> "setfuel". */
    public @NotNull String literal() {
        return name().replace("_", "").toLowerCase();
    }

}
