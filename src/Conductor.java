import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;

/**
 * The Conductor class is where everything happens. The conductor manages the players and what note is played when.
 * The main method found in this class is our programs "main thread". This class has a number of methods
 * associated with it. These methods include loading the song from a .txt file, validating that song,
 * and count the notes of that song to get an idea of how many players we need, to name a few.
 */
public class Conductor {

    //Main thread
    public static void main(String[] args) {
        //Validate the song. If the song is invalid, handle the error.
        if (!validateSong(args[0])) {
            System.err.println("Failed");
            System.exit(-1);
        }

        //Instance variables
        List<BellNote> song = loadSong(args[0]);

        List<Note> notes = countNotes(args[0]);

        //Start audio format
        final AudioFormat af =
                new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);

        try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            line.open();
            line.start();

            //Initialize players array based on how many notes there are in the song
            Player[] players = new Player[notes.size()];

            //Spawn our players and hand them their note
            //Each player has one note.
            for (int i = 0; i<notes.size(); i++) {
                players[i] = new Player(i, notes.get(i), line);
                players[i].startThread();
            }

            //For every bell note that is coming up, the conductor hands the player their note length
            //Once the note length has been passed, the player plays their note
            for (BellNote bn : song) {
                for (Player p : players) {
                    if (bn.note == p.getNote()) {
                        p.giveNoteLength(bn.length);
                        System.out.println("Playing note "+bn.note);
                        //Adding this break fixed the multiple notes running issue
                        break;
                    }
                }
            }

            line.drain();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        //Exit when we run out of lines
        System.exit(-1);

    }

    //Loads songs from .txt file to a list of bell notes
    //Checks if notes and notelengths are valid
    private static List<BellNote> loadSong(String filename) {
        //Instance variables
        List<BellNote> list = new ArrayList<BellNote>();
        String line = null;

        String[] correctNoteLength = {"1","2","4","8"};

        //fr and br to read .txt files
        try (FileReader fr = new FileReader(filename);
             BufferedReader br = new BufferedReader(fr);) {
            //While there are still lines to be read, continue
            while((line = br.readLine()) != null) {
                BellNote bn = new BellNote();
                String[] split = line.split(" ");

                boolean validNote = false;

                //Make sure notes match our Note class' enumeration
                for (Note note : Note.values()) {
                    if (note.name().toUpperCase().equals(split[0].toUpperCase())) {
                        validNote = true;
                        break;
                    }
                }

                if (!validNote) {
                    System.err.println("Your file contained an invalid note.");
                    System.exit(-1);
                }

                boolean validNoteLength = false;

                //Make sure note lengths match our NoteLength class' values.
                for (int i = 0; i<correctNoteLength.length && !validNoteLength; i++) {
                    if (split[1].equals(correctNoteLength[i])) {
                        validNoteLength = true;
                        break;
                    }
                }

                if (!validNoteLength) {
                    System.err.println("Your file contained an invalid note length.");
                    System.exit(-1);
                }

                //Run methods to change Strings to Note and NoteLength data types.
                bn.note = parseNote(split[0]);
                bn.length = parseNoteLength(split[1]);

                list.add(bn);
            }
        } catch (IOException ignored) {}

        return list;
    }

    //Takes String input passed in by loadSong() and returns the Note Class' enumeration in the Note data type
    private static Note parseNote(String note) {
        if (note == null) {
            return Note.INVALID;
        }

        //if the String input equals "(this)" return the Note type.
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

    //Takes String input passed in by loadSong() and returns the NoteLength Class' enumeration in the NoteLength data type
    private static NoteLength parseNoteLength(String nl) {
        if (nl == null) {
            return NoteLength.INVALID;
        }

        //if the String input equals "(this)" return the NoteLength type.
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

    //Run some checks on the validity of the .txt file given and handles errors
    private static boolean validateSong(String filename) {
        File file = new File(filename);
        //Check top-level directory for the filename which was given.
        boolean exists = file.exists();

        if (filename == null) {
            System.err.println("Make sure you pass a song (.txt) as an argument and that the file exists.");
            System.exit(-1);
        }
        else if (!exists) {
            System.err.println("The filename: "+filename+" does not exist.");
            System.exit(-1);
        }
        return true;
    }

    //countNotes() counts how many unique notes are in a given song
    //returns a list of notes which is later used to hire the right number of players
    public static List<Note> countNotes(String filename) {
        List<Note> list = new ArrayList<Note>();
        String line = null;

        //load the file, split each line up by the space, add to list as long as we haven't seen the note before
        try (FileReader fr = new FileReader(filename);
             BufferedReader br = new BufferedReader(fr);) {
            while((line = br.readLine()) != null) {
                String[] split = line.split(" ");

                //Check if we've seen that note before
                //If not, add it to the list
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