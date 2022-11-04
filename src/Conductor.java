import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;

public class Conductor {

    public static void main(String[] args) {
        if (!validateSong(args[0])) {
            System.err.println("Failed");
            System.exit(-1);
        }

        List<BellNote> song = loadSong(args[0]);

        List<Note> notes = countNotes(args[0]);

        final AudioFormat af =
                new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);

        try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            line.open();
            line.start();

            Player[] players = new Player[notes.size()];

            for (int i = 0; i<notes.size(); i++) {
                players[i] = new Player(i, notes.get(i), line);
                players[i].startThread();
            }

            for (BellNote bn : song) {
                for (Player p : players) {
                    if (bn.note == p.getNote()) {
                        p.giveNoteLength(bn.length);
                    }
                }
            }

            line.drain();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

    }

    private static List<BellNote> loadSong(String filename) {
        List<BellNote> list = new ArrayList<BellNote>();
        String line = null;

        try (FileReader fr = new FileReader(filename);
             BufferedReader br = new BufferedReader(fr);) {
            while((line = br.readLine()) != null) {
                BellNote bn = new BellNote();
                String[] split = line.split(" ");

                bn.note = parseNote(split[0]);
                bn.length = parseNoteLength(split[1]);

                list.add(bn);
            }
        } catch (IOException ignored) {}

        return list;
    }

    private static Note parseNote(String note) {
        if (note == null) {
            return Note.INVALID;
        }

        switch (note.toUpperCase().trim()) {
            case "A4":
                return Note.A4;
            case "A4S":
                return Note.A4S;
            case "B4":
                return Note.B4;
            case "C4":
                return Note.C4;
            case "C4S":
                return Note.C4S;
            case "D4":
                return Note.D4;
            case "D4S":
                return Note.D4S;
            case "E4":
                return Note.E4;
            case "F4":
                return Note.F4;
            case "F4S":
                return Note.F4S;
            case "G4":
                return Note.G4;
            case "G4S":
                return Note.G4S;
            case "A5":
                return Note.A5;
        }
        return Note.INVALID;
    }

    private static NoteLength parseNoteLength(String nl) {
        if (nl == null) {
            return NoteLength.INVALID;
        }

        switch (nl) {
            case "1":
                return NoteLength.WHOLE;
            case "2":
                return NoteLength.HALF;
            case "4":
                return NoteLength.QUARTER;
            case "8":
                return NoteLength.EIGHTH;
        }
        return NoteLength.INVALID;
    }

    private static boolean validateSong(String filename) {
        if (filename == null) {
            System.err.println("Make sure you pass a song (.txt) as an argument and that the file exists.");
            return false;
        }
        else {
            return true;
        }
    }

    public void getNextPlayer() {

    }

    public static List<Note> countNotes(String filename) {
        List<Note> list = new ArrayList<Note>();
        String line = null;

        try (FileReader fr = new FileReader(filename);
             BufferedReader br = new BufferedReader(fr);) {
            while((line = br.readLine()) != null) {
                String[] split = line.split(" ");

                for(int i = 0; i<split.length; i++) {
                    if (split[0] != split[i]) {
                        list.add(parseNote(split[0]));
                    }
                }
            }
        } catch (IOException ignored) {}

        return list;
    }
}