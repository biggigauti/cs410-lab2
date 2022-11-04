import javax.sound.sampled.SourceDataLine;

public class Player implements Runnable {

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

    public void startThread() {
        timeToWork = true;
        thread.start();
    }

    public void stopThread() {
        timeToWork = false;
    }

    private void playNote(NoteLength noteLength) {
        final int ms = Math.min(noteLength.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 50);
    }

    public Note getNote() {
        return note;
    }

    public synchronized void giveNoteLength(NoteLength length) {
        noteLength = length;
        notifyAll();

        while (noteLength != null) {
            try {
                wait();
            } catch (InterruptedException ignored) {}
        }
    }

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