package gitlet;

import javax.swing.*;
import java.io.File;
import java.util.List;
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
        // Set up initial Commit and safe it.
        Commit initialCommit = new Commit();
        initialCommit.saveToFile();
        // Set up initial Branch and safe it.
        Branch initialBranch = new Branch(initialCommit);
        initialBranch.saveToFile();
        // Fill in the HEAD and BRANCH files with current HEAD pointer and BRANCH.
        writeContents(HEAD, initialCommit.id);
        writeContents(BRANCH, initialBranch.branchName);
        // Set up the two Stages and safe them.
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
        addStage.saveToFile();
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
        checkGitletDir();
        // Set up the addStage and removeStage
        Stage addStage = Stage.getFromFile("addStage");
        Stage removeStage = Stage.getFromFile("removeStage");
        // Check for failure cases
        if (addStage.stageBlobMap.isEmpty() && removeStage.stageBlobMap.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        if (message == null) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        // Get the current commit.
        String currentCommitID = readContentsAsString(HEAD);
        Commit currentCommit = Commit.getFromFile(currentCommitID);
        // Set up and update the new commit.
        Commit newCommit = new Commit(message, currentCommit);
        addUpdate(newCommit, addStage);
        removeUpdate(newCommit, removeStage);
        newCommit.id = newCommit.createID();
        newCommit.saveToFile();
        // Change the HEAD pointer
        createNewFile(HEAD);
        writeContents(HEAD, newCommit.id);
        // Clear the Stages and safe the changes
        addStage.stageBlobMap.clear();
        removeStage.stageBlobMap.clear();
        addStage.saveToFile();
        removeStage.saveToFile();
        // Update the branch and safe the changes.
        Branch currentBranch = Branch.getFromFile(readContentsAsString(BRANCH));
        currentBranch.commitID = newCommit.id;
        currentBranch.saveToFile();
    }

    /** Updates the blobProjection in the new commit for files in addStage. */
    private static void addUpdate(Commit newCommit, Stage addStage) {
        // Loop through the addStage's mapping.
        // First case: if file is not included in current commit, add it in.
        for (String filepath : addStage.stageBlobMap.keySet()) {
            if (!(newCommit.blobProjection.containsKey(filepath))) {
                String blobID = addStage.stageBlobMap.get(filepath);
                newCommit.blobProjection.put(filepath, blobID);
            }
            // Second case: if file is included in the current commit, update its blob.
            for (String commitPath : newCommit.blobProjection.keySet()) {
                if (Objects.equals(commitPath, filepath)) {
                    String blobID = addStage.stageBlobMap.get(filepath);
                    newCommit.blobProjection.put(commitPath, blobID);
                }
            }
        }
    }

    /** Updates the blobProjection in the new commit for files in removeStage. */
    private static void removeUpdate(Commit newCommit, Stage removeStage) {
        for (String removePath : removeStage.stageBlobMap.keySet()) {
            for (String commitPath : newCommit.blobProjection.keySet()) {
                if (Objects.equals(removePath, commitPath)) {
                    newCommit.blobProjection.remove(commitPath);
                }
            }
        }
    }

    public static void logCommand() {
        checkGitletDir();
        String currentCommitID = readContentsAsString(HEAD);
        Commit currentCommit = Commit.getFromFile(currentCommitID);
        while (!currentCommit.parentList.isEmpty()) {
            currentCommit.printCommit();
            String parentString = currentCommit.parentList.get(0);
            Commit parentCommit = Commit.getFromFile(parentString);
            currentCommit = parentCommit;
        }
        currentCommit.printCommit();
    }

    public static void globalLogCommand() {
        checkGitletDir();
        List<String> commitList = plainFilenamesIn(COMMIT_FOLDER);
        assert commitList != null;
        for (String commitID : commitList) {
            Commit currentCommit = Commit.getFromFile(commitID);
            currentCommit.printCommit();
        }
    }

    public static void findCommand(String commitMessage) {
        checkGitletDir();
        List<String> commitList = plainFilenamesIn(COMMIT_FOLDER);
        boolean found = false;
        assert commitList != null;
        for (String commitID : commitList) {
            Commit currentCommit = Commit.getFromFile(commitID);
            if (Objects.equals(currentCommit.message, commitMessage)) {
                System.out.println(currentCommit.id);
                found = true;
            }
            if (!found) {
                System.out.println("Found no commit with that message.");
                System.exit(0);
            }
        }
    }

    public static void statusCommand() {
        checkGitletDir();
        printBranch();
        printStageFiles();
        printRemovedFiles();
        printModificationsNotStaged();
        printUntrackedFiles();
        System.out.println();
    }

    public static void printBranch() {
        System.out.println("=== Branches ===");
        String currentBranchName = readContentsAsString(BRANCH);
        List<String> branchList = plainFilenamesIn(BRANCH_FOLDER);
        assert branchList != null;
        for (String branchName : branchList) {
            if (Objects.equals(branchName, currentBranchName)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println();
    }

    public static void printStageFiles() {
        System.out.println("=== Staged Files ===");
        Stage addStage = Stage.getFromFile("addStage");
        for (String filename : addStage.stageBlobMap.keySet()) {
            File file = new File(filename);
            System.out.println(file.getName()); // KeySet obtained from TreeMap is in lexicographic order.
        }
        System.out.println();
    }

    public static void printRemovedFiles() {
        System.out.println("=== Removed Files ===");
        Stage removeStage = Stage.getFromFile("removeStage");
        for (String filename : removeStage.stageBlobMap.keySet()) {
            File file = new File(filename);
            System.out.println(file.getName());
        }
        System.out.println();
    }

    /** Check if a file in CWD is “modified but not staged”. */
    public static boolean modificationsNotStaged(File file) {
        boolean flag = false;
        Blob b = new Blob(file);
        Stage addStage = Stage.getFromFile("addStage");
        Stage removeStage = Stage.getFromFile("removeStage");
        //Case 1:Tracked in the current commit, changed in the working directory, but not staged.
        if (checkSameFile(file, b) &&
                !addStage.stageBlobMap.containsKey(file.getPath())) {
            flag = true;
        }
        //Case 2:Staged for addition, but with different contents than in the working directory.
        else if (addStage.stageBlobMap.containsKey(file.getPath()) &&
                !Objects.equals(addStage.stageBlobMap.get(file.getPath()), b.id)) {
            flag = true;
        }
        //Case 3:Staged for addition, but deleted in the working directory.
        else if (addStage.stageBlobMap.containsKey(file.getPath()) &&
                !file.exists()) {
            flag = true;
        }
        //Case 4:Not staged for removal, but tracked in the current commit and deleted from the working directory.
        else if (!removeStage.stageBlobMap.containsKey(file.getPath()) &&
                checkFileInCurrentCommit(file) && !file.exists()) {
            flag = true;
        }
        return flag;
    }

    public static void printModificationsNotStaged() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        List<String> nameList = plainFilenamesIn(CWD);
        assert nameList != null;
        for (String filename : nameList) {
            File file = new File(filename);
            if (modificationsNotStaged(file)) {
                System.out.println(file.getName());
            }
        }
        System.out.println();
    }

    public static boolean untrackedFiles(File file) {
        Stage addStage = Stage.getFromFile("addStage");
        if (!addStage.stageBlobMap.containsKey(file.getPath()) && !checkFileInCurrentCommit(file)) {
            return true;
        }
        return false;
    }

    public static void printUntrackedFiles() {
        System.out.println("=== Untracked Files ===");
        List<String> nameList = plainFilenamesIn(CWD);
        assert nameList != null;
        for (String filename : nameList) {
            File file = new File(filename);
            if (untrackedFiles(file)) {
                System.out.println(file.getName());
            }
        }
        System.out.println();
    }

    public static void branchCommand(String branchName) {
        checkGitletDir();
        String currentCommitID = readContentsAsString(HEAD);
        Commit currentCommit = Commit.getFromFile(currentCommitID);
        List<String> name = plainFilenamesIn(BRANCH_FOLDER);
        assert name != null;
        if (name.contains(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Branch newBranch = new Branch(branchName, currentCommit);
        newBranch.saveToFile();
    }


    public static void rmBranchCommand(String branchName) {
        checkGitletDir();
        List<String> name = plainFilenamesIn(BRANCH_FOLDER);
        assert name != null;
        if (!name.contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        String currentBranchName = readContentsAsString(BRANCH);
        if (currentBranchName.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        Branch branch = Branch.getFromFile(branchName);
        branch.commitID = null;
        File branchFile = join(BRANCH_FOLDER, branchName);
        restrictedDelete(branchFile);
    }

    public void checkoutCommand() {

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
