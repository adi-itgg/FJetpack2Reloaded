package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.enums;

import java.util.Arrays;
import java.util.List;

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

    public static final List<String> COMMANDS = Arrays.stream(values()).map(cmd -> cmd.name().replace("_", "").toLowerCase()).toList();

    public String cmd() {
        return this.name().replace("_", "").toLowerCase();
    }

    public boolean isEqual(String cmd) {
        return this.cmd().equalsIgnoreCase(cmd);
    }
}
