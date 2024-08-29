package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.io.Serializable;

public class Branch implements Serializable{
    public String branchName;
    /** Stores the ID of the last commit in this Branch. */
    public String commitID;

    public Branch(Commit commit) {
        this.branchName = "master";
        this.commitID= commit.id;
    }

    public Branch(String branchName, Commit commit) {
        this.branchName = branchName;
        this.commitID = commit.id;
    }

    public void saveToFile() {
        File outfile = join(Repository.BRANCH_FOLDER, this.commitID);
        writeObject(outfile, this);
    }

    public static Branch getFromFile(String currentBranchName) {
        File infile = join(Repository.BRANCH_FOLDER, currentBranchName);
        return readObject(infile, Branch.class);
    }
}
