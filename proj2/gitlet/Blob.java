package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Formatter;

public class Blob implements Serializable {
    /** The ID of this blob. */
    public String id;
    /** The file that has the content of this blob. */
    public File file;
    /** The path of the blob's corresponding file. */
    public String filePath;
    /** The byte array representation of the blob's file content. */
    public byte[] byteContent;

    public Blob(File file) {
        this.id = createID();
        this.file = file;
        this.filePath = file.getPath();
        this.byteContent = readContents(file);
    }

    /** Creates SHA1-ID for this blob. */
    private String createID() {
        return sha1((Object) byteContent, file.getName());
    }

    public void saveToFile() {
        File outfile = join(Repository.BLOB_FOLDER, this.id);
        writeObject(outfile, this);
    }

    public static Blob getFromFIle(String id) {
        File infile = join(Repository.BLOB_FOLDER, id);
        return readObject(infile, Blob.class);
    }

}
