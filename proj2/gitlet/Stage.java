package gitlet;

import org.antlr.v4.runtime.tree.Tree;

import java.io.File;
import static gitlet.Utils.*;
import java.io.Serializable;
import java.util.*;

public class Stage implements Serializable{
    public String stageName;
    public Map<String, String> stageBlobMap = new TreeMap<>();
    public List<String> stageBlobID = new ArrayList<>();

    public Stage(String stageName) {
        this.stageName = stageName;
    }

    public void saveToFile() {
        File outfile = join(Repository.STAGE_FOLDER, this.stageName);
        writeObject(outfile, this);
    }

    public static Stage fromFile(String stageName) {
        File infile = join(Repository.STAGE_FOLDER, stageName);
        return readObject(infile, Stage.class);
    }

}
