package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception.handler;

import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnusedReturnValue")
public class Catcher<T> {


    private T result;
    private @Nullable Throwable error;

    @SneakyThrows
    public static <T> @NotNull Catcher<T> create(@NotNull ActionResultHandler<T> action) {
        val job = new Catcher<T>();
        try {
            job.result = action.action();
        } catch (Throwable throwable) {
            job.error = throwable;
        }
        return job;
    }

    public static @NotNull Catcher<Nothing> createVoid(@NotNull ActionVoidHandler action) {
        return Catcher.create(() -> {
            action.action();
            return Nothing.nothing();
        });
    }

    private static <T> @NotNull Catcher<T> create(@NotNull ActionResultHandler<T> action, Throwable throwable) {
        val job = create(action);
        job.error = throwable;
        return job;
    }

    public <R> @NotNull Catcher<R> map(@NotNull ActionResultReturnHandler<T, R> action) {
        if (error != null)
            return create(() -> null, error);
        return create(() -> action.action(result), error);
    }

    public @NotNull Catcher<T> onSuccess(@NotNull ActionHandler<T> handler) {
        if (error == null)
            try {
                handler.handle(result);
            } catch (Throwable e) {
                error = e;
            }
        return this;
    }

    public @NotNull Catcher<T> onFailure(@NotNull ActionHandler<Throwable> handler) {
        if (error != null)
            try {
                handler.handle(error);
            } catch (Throwable e) {
                error = e;
            }
        return this;
    }

    @SneakyThrows
    public @NotNull Catcher<T> onCompleted(ActionVoidHandler handler) {
        handler.action();
        return this;
    }

    public @NotNull T getOrDefault(T defaultValue) {
        if (error != null) return defaultValue;
        return result;
    }

    @SneakyThrows
    public @NotNull T getOrThrow() {
        if (error != null) throw error;
        return result;
    }

    @SneakyThrows
    public void throwIfError() {
        if (error != null) throw error;
    }

}
