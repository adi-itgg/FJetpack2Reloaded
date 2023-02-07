package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler;

@FunctionalInterface
public interface ActionResultHandler<T> {
    T action() throws Throwable;
}
