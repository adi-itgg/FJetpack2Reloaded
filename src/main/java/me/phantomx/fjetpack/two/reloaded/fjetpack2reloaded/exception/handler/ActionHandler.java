package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler;

@FunctionalInterface
public interface ActionHandler<T> {
    void handle(T result) throws Throwable;
}

