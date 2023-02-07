package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception;


import lombok.SneakyThrows;

public class IMessageException extends Throwable {

    private static IMessageException instance;

    private static IMessageException getInstance() {
        if (instance == null) instance = new IMessageException();
        return instance;
    }

    @SneakyThrows
    public static void send() {
        throw getInstance();
    }

}
