package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.io.Serializable;

public class Branch implements Serializable{
    private String branchName;
    /** Stores the ID of the last commit in this Branch. */
    private String commitID;

    public Branch(String branchName) {
        this.branchName= branchName;
    }
}
