package me.phantomx.fjetpack.two.reloaded.fjetpack2reloaded.domain.flight;

/** Outcome of "can this player fly with this item right now?". */
public enum FlightCheck {

    /** Yes. */
    ALLOWED,
    /** The item is not a jetpack of this player: keep looking at the other slots, silently. */
    NOT_A_JETPACK,
    /** Missing permission; the presentation layer shows the no-permission message. */
    NO_PERMISSION,
    /** Refused by a protection rule or by fuel; the player has already been told why. */
    BLOCKED

}
