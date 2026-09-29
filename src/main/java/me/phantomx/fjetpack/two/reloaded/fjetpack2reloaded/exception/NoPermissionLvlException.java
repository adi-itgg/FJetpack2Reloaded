package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception;

/**
 * The sender lacks a permission that the requested operation requires.
 * <p>
 * Unchecked on purpose: every thrower is a command or event handler whose entry point already
 * translates this into a message for the sender, so the caller never has to declare it.
 */
public class NoPermissionLvlException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NoPermissionLvlException() {
        super("missing permission");
    }

    /** Bail out of the current command/event with this signal. */
    public static void throwNow() {
        throw new NoPermissionLvlException();
    }

}
