package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception;

import lombok.SneakyThrows;

public class NoPermissionLvlException extends Throwable {

    private static NoPermissionLvlException instance;

    private static NoPermissionLvlException getInstance() {
        if (instance == null) instance = new NoPermissionLvlException();
        return instance;
    }

    @SneakyThrows
    public static void send() {
        throw getInstance();
    }

}
