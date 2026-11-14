package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The sender lacks a permission that the requested operation requires.
 * <p>
 * Unchecked on purpose: every thrower is a command or event handler whose entry point already
 * translates this into a message for the sender, so the caller never has to declare it.
 * <p>
 * Carries the node that is missing and what the sender was doing with it, so the entry point can say
 * which permission to grant instead of repeating a bare "missing permission".
 */
public class NoPermissionLvlException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * @param permission the node the sender lacks, or null when the throw site could not resolve it
     * @param action     what the sender was trying to do, e.g. "running /fj2r setfuel"
     */
    public NoPermissionLvlException(@Nullable String permission, @Nullable String action) {
        super(describe(permission, action));
    }

    /** "missing permission 'fjetpack2reloaded.set' while running /fj2r set". */
    private static @NotNull String describe(@Nullable String permission, @Nullable String action) {
        String node = permission == null ? "(not resolved)" : "'" + permission + "'";
        return "missing permission " + node + (action == null ? "" : " while " + action);
    }

    /** Bail out of the current command/event: the sender lacks {@code permission} for {@code action}. */
    public static void throwNow(@Nullable String permission, @Nullable String action) {
        throw new NoPermissionLvlException(permission, action);
    }

    public static void throwNow(@Nullable String permission) {
        throw new NoPermissionLvlException(permission, null);
    }

}
