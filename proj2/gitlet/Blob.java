package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Formatter;

public class Blob implements Serializable {
    /** List all instance variables here. */
    static final File BLOB_FOLDER = join(".gitlet", "objects", "blobs");
    /** The ID of this blob. */
    private String id;
    /** The name of this blob. */
    private String name;

    public Blob() {

    }
}
