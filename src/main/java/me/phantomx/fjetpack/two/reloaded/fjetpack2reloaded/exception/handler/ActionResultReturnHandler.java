package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler;

@FunctionalInterface
public interface ActionResultReturnHandler<T, R> {
    R action(T result) throws Throwable;
}
