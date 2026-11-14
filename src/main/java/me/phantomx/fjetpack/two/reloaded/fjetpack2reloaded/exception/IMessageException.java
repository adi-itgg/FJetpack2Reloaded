package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The operation already reported the problem to the sender; the handler must not report it again.
 * <p>
 * Unchecked on purpose: it travels from the use case to the Bukkit entry point, which catches it
 * and returns without sending anything.
 * <p>
 * Carries what was already reported, so a log line that follows can name the item instead of
 * repeating a bare "already messaged".
 */
public class IMessageException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IMessageException(@Nullable String reason) {
        super("already messaged: " + reason);
    }

    /** Stop the operation here: the sender has been told, and {@code reason} repeats what they were told. */
    public static void throwNow(@NotNull String reason) {
        throw new IMessageException(reason);
    }

}
