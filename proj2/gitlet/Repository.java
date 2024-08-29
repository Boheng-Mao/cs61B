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

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /**
     * Setting up the objects, blobs and commit directories.
     */
    public static final File OBJECT_DIR = join(GITLET_DIR, "objects");
    public static final File BLOB_FOLDER = join(OBJECT_DIR, "blobs");
    public static final File COMMIT_FOLDER = join(OBJECT_DIR, "commits");

    /**
     * Setting up the branch directory.
     */
    public static final File BRANCH_FOLDER = join(GITLET_DIR, "branches");
    /**
     * Setting up the stages' directory.
     */
    public static final File STAGE_FOLDER = join(GITLET_DIR, "stages");

    /**
     * Setting up HEAD and BRANCH files.
     */
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

    /** filename is in the form of "example.txt" for instance. */
    public static void addCommand(String filename) {
        checkGitletDir();
        File file = join(CWD, filename); // form the file paths from CWD for that file by joining.
        checkFileExists(file);
        Blob b = new Blob(file);
        Stage addStage = Stage.getFromFile("addStage");
        Stage removeStage = Stage.getFromFile("removeStage");
        // if file's content is same as the content in the current commit, don't put to addStage.
        if (checkSameFile(file, b)) {
            addStage.stageBlobMap.remove(file.getPath());
            addStage.saveToFile();
        }
        // if file is in remove stage, delete file from removeStage and put it to addStage.
        else if (removeStage.stageBlobMap.containsKey(file.getPath())) {
            removeStage.stageBlobMap.remove(file.getPath());
            removeStage.saveToFile();
            addStage.stageBlobMap.put(file.getPath(), b.id);
            addStage.saveToFile();
            b.saveToFile();
        }
        // Else, add the file to the addStage.
        addStage.stageBlobMap.put(file.getPath(), b.id);
        b.saveToFile();
    }

    public static void removeCommand(String filename) {
        checkGitletDir();
        File file = join(CWD, filename);
        Blob b = new Blob(file);
        Stage addStage = Stage.getFromFile("addStage");
        Stage removeStage = Stage.getFromFile("removeStage");
        // if file is staged to add, remove it from addStage.
        if (addStage.stageBlobMap.containsKey(file.getPath())) {
            addStage.stageBlobMap.remove(file.getPath());
            addStage.saveToFile();
            removeStage.stageBlobMap.put(file.getPath(), b.id);
            removeStage.saveToFile();
        }
        // if file is in current commit, put it in remove stage and delete it from CWD.
        else if (checkFileInCurrentCommit(file)) {
            removeStage.stageBlobMap.put(file.getPath(), b.id);
            if (file.exists()) {
                restrictedDelete(filename);
            }
        }
        else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    public static void commitCommand(String message) {
        
    }

    /** Checks if the current environment has been initialized a GITLET_DIR
     * if not, return error message and exit. */
    private static void checkGitletDir() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    /** Checks if the file's path is valid in the CWD. */
    private static void checkFileExists(File file) {
        if (!file.exists()) {
            System.out.println("File does not exits.");
            System.exit(0);
        }
    }

    /** Checks if the current working version of the file
     *  is identical to the version in the current commit. */
    private static boolean checkSameFile(File file, Blob blob) {
        String currentCommitID = readContentsAsString(HEAD);
        Commit currentCommit = Commit.getFromFile(currentCommitID);
        if (currentCommit.blobProjection.containsKey(file.getPath())) {
            return Objects.equals(currentCommit.blobProjection.get(file.getPath()), blob.id);
        }
        return false;
    }

    /** Checks if the file is tracked in the current commit. */
    private static boolean checkFileInCurrentCommit(File file) {
        String currentCommitID = readContentsAsString(HEAD);
        Commit currentCommit = Commit.getFromFile(currentCommitID);
        if (currentCommit.blobProjection.containsKey(file.getPath())) {
            return true;
        }
        return false;
    }

}
