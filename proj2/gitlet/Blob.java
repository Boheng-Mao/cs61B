package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Formatter;

public class Blob implements Serializable {
    /** The ID of this blob. */
    private String id;
    /** The name of this blob. */
    private String name;

    public Blob() {

    }
}
