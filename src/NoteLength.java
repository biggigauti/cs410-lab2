/**
 * The NoteLength class includes an enumeration of each different length a note can take.
 */
public enum NoteLength {
    INVALID(0.0f),
    WHOLE(1.0f),
    HALF(0.5f),
    QUARTER(0.25f),
    EIGHTH(0.125f);

    //Instance variables
    private final int timeMs;

    //Calculates note length
    private NoteLength(float length) {
        timeMs = (int)(length * Note.MEASURE_LENGTH_SEC * 1000);
    }

    public int timeMs() {
        return timeMs;
    }
}

