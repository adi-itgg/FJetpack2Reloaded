package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight;

/** Outcome of "may this jetpack be used in this world?". */
public enum WorldAccess {

    /** Yes. */
    ALLOWED,
    /** The world is named in {@code BlockedWorlds}. */
    BLOCKED,
    /** The world is not named in {@code AllowedWorlds}, which is not empty. */
    NOT_ALLOWED

}
