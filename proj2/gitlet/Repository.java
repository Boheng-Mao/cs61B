package gitlet;

import javax.swing.*;
import java.io.File;
import java.util.Objects;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  The structure of a gitlet repository is as follows:
 *
 *  .gitlet/
 *      -objects/
 *          -commits/ -- folder containing persisted data for commits
 *          -blobs/ -- folder containing persisted data for blobs.
 *      -branches/
 *          -master -- file for the master branch
 *          -other -- file for other branches
 *      -HEAD -- file tracking the HEAD pointer
 *      -BRANCH -- file tracking the current branch
 *      -Stages/
 *          -addStage -- file containing the files in addStage area
 *          -removeStage -- file containing the files in removeStage area
 *
 *  @author Boheng Mao
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** Setting up the objects, blobs and commit directories. */
    public static final File OBJECT_DIR = join(GITLET_DIR, "objects");
    public static final File BLOB_FOLDER = join(OBJECT_DIR, "blobs");
    public static final File COMMIT_FOLDER = join(OBJECT_DIR,"commits");

    /** Setting up the branch directory. */
    public static final File BRANCH_FOLDER = join(GITLET_DIR, "branches");
    /** Setting up the stages' directory. */
    public static final File STAGE_FOLDER = join(GITLET_DIR, "stages");

    /** Setting up HEAD and BRANCH files. */
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    public static final File BRANCH = join(GITLET_DIR, "BRANCH");

    public static void setupPersistence() {
        GITLET_DIR.mkdir();
        OBJECT_DIR.mkdir();
        BLOB_FOLDER.mkdir();
        COMMIT_FOLDER.mkdir();
        BRANCH_FOLDER.mkdir();
        STAGE_FOLDER.mkdir();
    }

    public static void initCommand() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        setupPersistence();
        Commit initialCommit = new Commit();
        initialCommit.saveToFile();
        Branch initialBranch = new Branch("master");
        initialBranch.commitID = initialCommit.id;
        writeContents(HEAD, initialCommit.id);
        writeContents(BRANCH, initialBranch.branchName);
        Stage addStage = new Stage("addStage");
        Stage removeStage = new Stage("removeStage");
        addStage.saveToFile();
        removeStage.saveToFile();
    }

    public static void addCommand(String filename) {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        if (!checkFileExists(filename, CWD)) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        File file = join(CWD, filename);
        Blob b = new Blob(file);
        Stage addStage = Stage.fromFile("addStage");
        Stage removeStage = Stage.fromFile("removeStage");
        if (removeStage.stageBlobMap.containsKey(filename)) {
            //TODO: do not remove the file in this case, and add the file to addStage.
        }
        if (addStage.stageBlobMap.containsKey(filename)) {
            if (Objects.equals(addStage.stageBlobMap.get(filename), b.id)) {
                addStage.stageBlobMap.remove(filename);
            } else {
                addStage.stageBlobMap.put(filename, b.id);
                b.saveToFile();
            }
        }
    }

    private static boolean checkFileExists(String filename, File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            boolean isfound = false;
            for (File file : files) {
                if (file.isFile() && file.getName().equals(filename)) {
                    isfound = true;
                    return isfound;
                }
            }
        }
        return false;
    }

}
