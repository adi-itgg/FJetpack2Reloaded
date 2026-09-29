package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.exception;

/**
 * The operation already reported the problem to the sender; the handler must not report it again.
 * <p>
 * Unchecked on purpose: it travels from the use case to the Bukkit entry point, which catches it
 * and returns without sending anything.
 */
public class IMessageException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IMessageException() {
        super("already messaged");
    }

    /** Stop the operation here: the user has been told what happened. */
    public static void throwNow() {
        throw new IMessageException();
    }

}
