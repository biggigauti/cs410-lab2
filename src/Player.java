import javax.sound.sampled.SourceDataLine;

/**
 * The Player class implements runnable or threads so that each player can exist on their own and have a note
 * assigned to them. This class has a number of methods associated with it to ensure that the players can receive
 * their note and note length from the conductor. The player is fully in charge of playing their own note when
 * the conductor hands them their note.
 */
public class Player implements Runnable {

    //Instance variables
    private volatile boolean timeToWork;

    private final Thread thread;

    private NoteLength noteLength;

    private SourceDataLine line;

    private Note note;

    //BellNote or just Note? Will Conductor pass note lengths?
    public Player(int threadNum, Note note, SourceDataLine line) {

        thread = new Thread(this, "Player[" + threadNum + "]");
        this.line = line;
        this.note = note;
    }

    //Starts the thread when run.
    public void startThread() {
        timeToWork = true;
        thread.start();
    }

    //Stops the thread when run
    public void stopThread() {
        timeToWork = false;
    }

    //This is run when a note needs to be played. The method receives a note length and passes that length on
    //to our audio player in the correct format.
    private void playNote(NoteLength noteLength) {
        final int ms = Math.min(noteLength.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 50);
    }

    public Note getNote() {
        return note;
    }

    //Method that allows the conductor to pass in a note length. Once that happens, threads are woken up with notify.
    public synchronized void giveNoteLength(NoteLength length) {
        noteLength = length;
        notifyAll();

        //Keep checking to see if note null. If it is, wait. If not, exit while loop and move forward.
        while (noteLength != null) {
            try {
                wait();
            } catch (InterruptedException ignored) {}
        }
    }

    //This runs when the thread is started. The run() method explains the lifetime of our players.
    //It is simply a loop of playNote() when you are woken up and then return to a waiting state.
    @Override
    public void run() {
        synchronized(this) {
            while (timeToWork) {
                while (noteLength == null) {
                    try {
                        wait();
                    } catch (InterruptedException ignored) {}
                }
                playNote(noteLength);
                noteLength = null;
                notifyAll();
            }
        }
    }
}