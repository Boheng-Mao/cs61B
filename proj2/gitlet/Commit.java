package gitlet;

// TODO: any imports you need here

import java.io.File;
import static gitlet.Utils.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Formatter;

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

    /** The Folder that commits are located. */
    static final File COMMIT_FOLDER = join(".gitlet", "objects","commits");
    /** The message of this Commit. */
    private String message;
    /** The date of this commit. */
    private Date timestamp;
    /** The ID of this commit. */
    private String id;
    /** The two parents of this commit stored as ID. */
    private String parent1;
    private String parent2;
    /** A mapping of file names to blob references as ID (TreeMap?). */

    public Commit(String message, Date timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

}
