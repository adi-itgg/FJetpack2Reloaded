package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler;

import org.jetbrains.annotations.Nullable;

public class Nothing {
    private static @Nullable Nothing instance;
    public static Nothing nothing() {
        if (instance == null) instance = new Nothing();
        return instance;
    }
}

