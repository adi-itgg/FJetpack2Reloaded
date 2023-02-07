package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception;

import org.jetbrains.annotations.NotNull;

public class MessageLvlException extends Throwable {

    public MessageLvlException(@NotNull String message, Object @NotNull ... args) {
        super(String.format(message, args));
    }

}
