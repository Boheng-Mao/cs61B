package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.io.Serializable;

public class Branch implements Serializable{
    public String branchName;
    /** Stores the ID of the last commit in this Branch. */
    public String commitID;

    public Branch(String branchName) {
        this.branchName= branchName;
    }

    public void saveToFile() {
        File outfile = join(Repository.BRANCH_FOLDER, this.commitID);
        writeObject(outfile, this);
    }
}
