package gitlet;

// TODO: any imports you need here

import java.io.File;
import static gitlet.Utils.*;
import java.io.Serializable;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    public String message;
    /** The date of this commit. */
    public Date currentTime;
    public String timeStamp;
    /** The ID of this commit. */
    public String id;
    /** The two parents of this commit stored as ID. */
    public List<String> parentList = new ArrayList<>();
    /** A mapping of file paths to blob ids (TreeMap?). */
    public Map<String, String> blobProjection = new TreeMap<>();
    /** A list containing the blobs in this commit. */
    public List<String> blobList = new ArrayList<>();

    /** Creates the initial commit. */
    public Commit() {
        this.message = "initial commit";
        this.currentTime = new Date(0);
        this.timeStamp = createTimeStamp(currentTime);
        this.id = createID();
    }

    public Commit(String message, Date timestamp) {
        this.message = message;
        this.currentTime = timestamp;
    }

    /** Create a timestamp given a Date object for each commit. */
    private String createTimeStamp(Date currentTime) {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return dateFormat.format(currentTime);
    }

    /** Serialize and safe the commit object to the commits directory. */
    public void saveToFile() {
        File outfile = join(Repository.COMMIT_FOLDER, this.id);
        writeObject(outfile, this);
    }

    public static Commit getFromFile(String id) {
        File infile = join(Repository.COMMIT_FOLDER, id);
        return readObject(infile, Commit.class);
    }

    /** Create and return a ID for each commit. */
    private String createID() {
        return sha1(message, parentList.toString(), blobList.toString(), timeStamp);
    }
}
