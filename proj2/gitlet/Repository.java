package gitlet;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
        if (removeStage.stageBlobMap.containsKey(file.getPath())) {
            removeStage.stageBlobMap.remove(file.getPath());
            removeStage.saveToFile();
            b.saveToFile();
        }
        // Else, add the file to the addStage.
        if (!checkSameFile(file, b) && !removeStage.stageBlobMap.containsKey(file.getPath())) {
            addStage.stageBlobMap.put(file.getPath(), b.id);
            addStage.saveToFile();
            b.saveToFile();
        }
    }

    public static void removeCommand(String filename) {
        checkGitletDir();
        File file = join(CWD, filename);
        Stage addStage = Stage.getFromFile("addStage");
        Stage removeStage = Stage.getFromFile("removeStage");
        // if file is staged to add, remove it from addStage.
        if (addStage.stageBlobMap.containsKey(file.getPath())) {
            addStage.stageBlobMap.remove(file.getPath());
            addStage.saveToFile();
        }
        // if file is in current commit, put it in remove stage and delete it from CWD.
        else if (checkFileInCurrentCommit(file)) {
            String currentCommitID = readContentsAsString(HEAD);
            Commit currentCommit = Commit.getFromFile(currentCommitID);
            String blobID = currentCommit.blobProjection.get(file.getPath());
            Blob b = Blob.getFromFIle(blobID);
            removeStage.stageBlobMap.put(file.getPath(), b.id);
            removeStage.saveToFile();
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
        if (Objects.equals(message, "")) {
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
        List<String> toRemove = new ArrayList<>();
        for (String removePath : removeStage.stageBlobMap.keySet()) {
            for (String commitPath : newCommit.blobProjection.keySet()) {
                if (Objects.equals(removePath, commitPath)) {
                    toRemove.add(removePath);
                }
            }
        }
        for (String removePath : toRemove) {
            newCommit.blobProjection.remove(removePath);
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
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void statusCommand() {
        checkGitletDir();
        printBranch();
        printStageFiles();
        printRemovedFiles();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
        //printModificationsNotStaged();
        //printUntrackedFiles();
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
            File file = join(CWD, filename);
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
            File file = join(CWD, filename);
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
        branchFile.delete();
    }

    public static void checkoutCommand1(String filename) {
        checkGitletDir();
        String currentCommitID = readContentsAsString(HEAD);
        Commit currentCommit = Commit.getFromFile(currentCommitID);
        checkoutCommandHelper(filename, currentCommit);
    }

    /** Checkout the file with given filename in the given commit. */
    private static void checkoutCommandHelper(String filename, Commit commit) {
        File file = join(CWD, filename);
        if (!commit.blobProjection.containsKey(file.getPath())) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String blobID = commit.blobProjection.get(file.getPath());
        Blob b = Blob.getFromFIle(blobID);
        writeContents(file, (Object) b.byteContent);
    }

    public static void checkoutCommand2(String commitID, String filename) {
        checkGitletDir();
        List<String> nameList = plainFilenamesIn(COMMIT_FOLDER);
        assert nameList != null;
        if (commitID.length() < 40) {
            boolean exist = false;
            for (String id : nameList) {
                String idShort = id.substring(0, commitID.length());
                if (idShort.equals(commitID)) {
                    exist = true;
                    Commit commit = Commit.getFromFile(id);
                    checkoutCommandHelper(filename, commit);
                    break;
                }
            }
            if (!exist) {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
        }
        else {
            if (!nameList.contains(commitID)) {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
            Commit commit = Commit.getFromFile(commitID);
            checkoutCommandHelper(filename, commit);
        }
    }

    public static void checkoutCommand3(String branchName) {
        checkGitletDir();
        if (!checkBranchExists(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        String currentBranchName = readContentsAsString(BRANCH);
        if (currentBranchName.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        checkout3BranchHelper(branchName);
    }

    /** Perform checkout command given a branch. */
    private static void checkout3BranchHelper(String branchName) {
        Branch givenBranch = Branch.getFromFile(branchName);
        Commit head = Commit.getFromFile(givenBranch.commitID);
        Branch currentBranch = Branch.getFromFile(readContentsAsString(BRANCH));
        Commit currentHead = Commit.getFromFile(currentBranch.commitID);
        Set<String> filePathSet = head.blobProjection.keySet();
        // Iterate over all files in head commit and puts them in CWD, overwriting if needed .
        for (String filepath : filePathSet) {
            File file = new File(filepath);
            Blob b = Blob.getFromFIle(head.blobProjection.get(filepath));
            if (file.exists() && untrackedFileInGivenCommit(file, currentHead)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
            writeContents(file, (Object) b.byteContent);
        }
        // Any files that are tracked in the current branch
        // but are not present in the checked-out branch are deleted.
        Set<String> currentPathSet = currentHead.blobProjection.keySet();
        for (String currentFilePath : currentPathSet) {
            File file = new File(currentFilePath);
            if (!untrackedFileInGivenCommit(file, currentHead) && untrackedFileInGivenCommit(file, head)) {
                file.delete();
            }
        }
        // Clear Staging area.
        Stage addStage = Stage.getFromFile("addStage");
        Stage removeStage = Stage.getFromFile("removeStage");
        addStage.stageBlobMap.clear();
        removeStage.stageBlobMap.clear();
        addStage.saveToFile();
        removeStage.saveToFile();
        // Set new current Branch and current HEAD.
        HEAD.delete();
        createNewFile(HEAD);
        writeContents(HEAD, head.id);
        BRANCH.delete();
        createNewFile(BRANCH);
        writeContents(BRANCH, branchName);
    }


    /** Check if branch with name branchName exists. */
    private static boolean checkBranchExists(String branchName) {
        List<String> nameList = plainFilenamesIn(BRANCH_FOLDER);
        assert nameList != null;
        for (String name : nameList) {
            if (Objects.equals(name, branchName)) {
                return true;
            }
        }
        return false;
    }

    public static void resetCommand(String commitID) {
        List<String> nameList = plainFilenamesIn(COMMIT_FOLDER);
        Branch currentBranch = Branch.getFromFile(readContentsAsString(BRANCH));
        Commit currentHead = Commit.getFromFile(currentBranch.commitID);
        assert nameList != null;
        if (!nameList.contains(commitID)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = Commit.getFromFile(commitID);
        Set<String> pathSet = commit.blobProjection.keySet();
        for (String path : pathSet) {
            File file = new File(path);
            if (file.exists() && untrackedFileInGivenCommit(file, currentHead)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
            checkoutCommandHelper(file.getName(), commit);
        }
        Set<String> currentPathSet = currentHead.blobProjection.keySet();
        for (String currentFilePath : currentPathSet) {
            File file = new File(currentFilePath);
            if (!untrackedFileInGivenCommit(file, currentHead) && untrackedFileInGivenCommit(file, commit)) {
                file.delete();
            }
        }
        // Change the HEAD pointer.
        HEAD.delete();
        createNewFile(HEAD);
        writeContents(HEAD, commitID);
        // Change the current BRANCH to point to commit with commitID.
        currentBranch.commitID = commit.id;
        currentBranch.saveToFile();
        // Clear Staging area.
        Stage addStage = Stage.getFromFile("addStage");
        Stage removeStage = Stage.getFromFile("removeStage");
        addStage.stageBlobMap.clear();
        removeStage.stageBlobMap.clear();
        addStage.saveToFile();
        removeStage.saveToFile();
    }

    public static void mergeCommand(String branchName) {
        checkGitletDir();
        // Get the addStage and removeStage
        Stage addStage = Stage.getFromFile("addStage");
        Stage removeStage = Stage.getFromFile("removeStage");
        filePresentForStage();
        if (!checkBranchExists(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        mergeWithItself(branchName);
        easyMerge(branchName);
        // Get the three commits
        Commit splitCommit = findSplitPoint(branchName);
        Commit branchCommit = Commit.getFromFile(Branch.getFromFile(branchName).commitID);
        Commit currentCommit = Commit.getFromFile(readContentsAsString(HEAD));
        // Create a list with all the paths of files recorded in these three commits.
        Set<String> filePathList = new TreeSet<>();
        filePathList.addAll(splitCommit.blobProjection.keySet());
        filePathList.addAll(branchCommit.blobProjection.keySet());
        filePathList.addAll(currentCommit.blobProjection.keySet());
        // Iterate through all the files, determine the output result of merge on that file.
        boolean conflictEncountered = false;
        for (String filepath : filePathList) {
            String resultVersion = fileResult(branchName, filepath);
            if (resultVersion.equals("Conflict")) {
                conflictEncountered = true;
                File f = new File(filepath);
                String currentContent;
                String branchContent;
                if (currentCommit.blobProjection.containsKey(filepath)) {
                    byte[] currentByteContent = Blob.getFromFIle(currentCommit.blobProjection.get(filepath)).byteContent;
                    currentContent = new String(currentByteContent, StandardCharsets.UTF_8);
                } else {
                    currentContent = "";
                }
                if (branchCommit.blobProjection.containsKey(filepath)) {
                    byte[] branchByteContent = Blob.getFromFIle(branchCommit.blobProjection.get(filepath)).byteContent;
                    branchContent = new String(branchByteContent, StandardCharsets.UTF_8);
                } else {
                    branchContent = "";
                }
                String output = "<<<<<<< HEAD\n" + currentContent + "=======\n" + branchContent + ">>>>>>>\n";
                if (f.exists()) {
                    f.delete();
                }
                createNewFile(f);
                writeContents(f, output);
                Blob blob = new Blob(f);
                blob.saveToFile();
                addStage.stageBlobMap.put(f.getPath(), blob.id);
                addStage.saveToFile();
            }
            else if (resultVersion.equals("Current")) {
                File f = new File(filepath);
                if (currentCommit.blobProjection.containsKey(filepath)) {
                    Blob blob = Blob.getFromFIle(currentCommit.blobProjection.get(filepath));
                    addStage.stageBlobMap.put(f.getPath(), blob.id);
                    addStage.saveToFile();
                } else {
                    removeStage.stageBlobMap.put(filepath, branchCommit.blobProjection.get(filepath));
                    removeStage.saveToFile();
                }
            }
            else if (resultVersion.equals("Branch")) {
                File f = new File(filepath);
                if (branchCommit.blobProjection.containsKey(filepath)) {
                    Blob blob = Blob.getFromFIle(branchCommit.blobProjection.get(filepath));
                    addStage.stageBlobMap.put(f.getPath(), blob.id);
                    addStage.saveToFile();
                } else {
                    removeStage.stageBlobMap.put(filepath, currentCommit.blobProjection.get(filepath));
                    removeStage.saveToFile();
                }
            }
            untrackedFileInWay(filepath);
        }
        for (String path : addStage.stageBlobMap.keySet()) {
            File f = new File(path);
            Blob b = Blob.getFromFIle(addStage.stageBlobMap.get(path));
            writeContents(f, (Object) b.byteContent);
        }
        for (String path : removeStage.stageBlobMap.keySet()) {
            File f = new File(path);
            f.delete();
        }
        mergeCommit(branchName);
        if (conflictEncountered) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** A separate easy case for merging. */
    private static void easyMerge(String branchName) {
        Commit splitCommit = findSplitPoint(branchName);
        Commit branchCommit = Commit.getFromFile(Branch.getFromFile(branchName).commitID);
        Commit currentCommit = Commit.getFromFile(readContentsAsString(HEAD));
        if (splitCommit.id.equals(branchCommit.id)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        else if (splitCommit.id.equals(currentCommit.id)) {
            checkoutCommand3(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
    }

    /** Create a new commit for merging. */
    private static void mergeCommit(String branchName) {
        Stage addStage = Stage.getFromFile("addStage");
        Stage removeStage = Stage.getFromFile("removeStage");
        String message = "Merged " + branchName + " into " + readContentsAsString(BRANCH) + ".";
        Commit mergeCommit = new Commit(message, Commit.getFromFile(readContentsAsString(HEAD)));
        addUpdate(mergeCommit, addStage);
        removeUpdate(mergeCommit, removeStage);
        // Add the second parent into the parentList's second position.
        mergeCommit.parentList.add(Branch.getFromFile(branchName).commitID);
        mergeCommit.saveToFile();
        // Edit the HEAD pointer.
        createNewFile(HEAD);
        writeContents(HEAD, mergeCommit.id);
        // Clear the Stages and safe the changes
        addStage.stageBlobMap.clear();
        removeStage.stageBlobMap.clear();
        addStage.saveToFile();
        removeStage.saveToFile();
        // Update the branch and safe the changes.
        Branch currentBranch = Branch.getFromFile(readContentsAsString(BRANCH));
        currentBranch.commitID = mergeCommit.id;
        currentBranch.saveToFile();
    }

    /** Checks if there are any files in addStage or removeStage. */
    private static void filePresentForStage() {
        Stage addStage = Stage.getFromFile("addStage");
        Stage removeStage = Stage.getFromFile("removeStage");
        if (!addStage.stageBlobMap.isEmpty() || !removeStage.stageBlobMap.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
    }

    /** Checks if attempting to merge a branch with itself. */
    private static void mergeWithItself(String branchName) {
        Stage addStage = Stage.getFromFile("addStage");
        Stage removeStage = Stage.getFromFile("removeStage");
        if (Objects.equals(branchName, readContentsAsString(BRANCH))) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    /** Checks if an untracked file in the current commit
     * would be overwritten or deleted by the merge. */
    private static void untrackedFileInWay(String filepath) {
        Commit currentCommit = Commit.getFromFile(readContentsAsString(HEAD));
        File f = new File(filepath);
        Stage addStage = Stage.getFromFile("addStage");
        Stage removeStage = Stage.getFromFile("removeStage");
        if (f.exists() && !checkFileInCurrentCommit(f)) {
            if (addStage.stageBlobMap.containsKey(filepath) || removeStage.stageBlobMap.containsKey(filepath)) {
                addStage.stageBlobMap.clear();
                removeStage.stageBlobMap.clear();
                addStage.saveToFile();
                removeStage.saveToFile();
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    /** Helper method that determines the result version after merge on file with filepath. */
    private static String fileResult(String branchName, String filepath) {
        Commit splitCommit = findSplitPoint(branchName);
        Commit branchCommit = Commit.getFromFile(Branch.getFromFile(branchName).commitID);
        Commit currentCommit = Commit.getFromFile(readContentsAsString(HEAD));
        // Check if file exists in these three target commits. If not, set an BlobID of null temporarily.
        Map<String, String> idMap = new TreeMap<>();    // IDs will be stored in this TreeMap.
        if (!splitCommit.blobProjection.containsKey(filepath)) {
            String splitID = null;
            idMap.put("splitID", splitID);
        } else {
            String splitID = splitCommit.blobProjection.get(filepath);
            idMap.put("splitID", splitID);
        }
        if (!branchCommit.blobProjection.containsKey(filepath)) {
            String branchID = null;
            idMap.put("branchID", branchID);
        } else {
            String branchID = branchCommit.blobProjection.get(filepath);
            idMap.put("branchID", branchID);
        }
        if (!currentCommit.blobProjection.containsKey(filepath)) {
            String currentID = null;
            idMap.put("currentID", currentID);
        } else {
            String currentID = currentCommit.blobProjection.get(filepath);
            idMap.put("currentID", currentID);
        }
        // Find the result of version of file after merge.
        // If none of the ids are the same, this is a merge conflict for this file.
        if (!Objects.equals(idMap.get("splitID"), idMap.get("branchID"))
                && !Objects.equals(idMap.get("branchID"), idMap.get("currentID"))
                && !Objects.equals(idMap.get("currentID"), idMap.get("splitID"))) {
            return "Conflict";
        }
        // If all of them are the same, there are nothing to change for this file after merge.
        else if (Objects.equals(idMap.get("splitID"), idMap.get("branchID"))
                && Objects.equals(idMap.get("branchID"), idMap.get("currentID"))
                && Objects.equals(idMap.get("currentID"), idMap.get("splitID"))) {
            return "No change";
        }
        else {
            if (Objects.equals(idMap.get("splitID"), idMap.get("branchID"))
                    && !Objects.equals(idMap.get("branchID"), idMap.get("currentID"))) {
                return "Current";
            }
            else if (Objects.equals(idMap.get("splitID"), idMap.get("currentID"))
                    && !Objects.equals(idMap.get("branchID"), idMap.get("currentID"))) {
                return "Branch";
            }
            else {
                return "No change";
            }
        }
    }

    /** Find the split point of current branch and given branch for the merge command. */
    private static Commit findSplitPoint(String givenBranchName) {
        Branch givenBranch = Branch.getFromFile(givenBranchName);
        Branch currentBranch = Branch.getFromFile(readContentsAsString(BRANCH));
        Map<String, Integer> routeGivenBranch = findPathToInitialCommit(Commit.getFromFile(givenBranch.commitID));
        Map<String, Integer> routeCurrentBranch = findPathToInitialCommit(Commit.getFromFile(currentBranch.commitID));
        String splitCommitID = "";
        int minValue = Integer.MAX_VALUE;
        for (String commit: routeGivenBranch.keySet()) {
            if (routeCurrentBranch.containsKey(commit)) {
                if (routeCurrentBranch.get(commit) < minValue) {
                    splitCommitID = commit;
                    minValue = routeCurrentBranch.get(commit);
                }
            }
        }
        return Commit.getFromFile(splitCommitID);
    }

    /** Returns a map that contains paths from COMMIT to initial Commit. */
    /** The key is the Commit ids in the paths, the value is the distance from COMMIT. */
    private static Map<String, Integer> findPathToInitialCommit(Commit commit) {
        Map<String, Integer> route = new TreeMap<>();
        Queue<String> commitQueue = new ArrayDeque<>();
        commitQueue.add(commit.id);
        route.put(commit.id, 0);
        while (!commitQueue.isEmpty()) {
            String commitID = commitQueue.poll();
            Commit currentCommit = Commit.getFromFile(commitID);
            for (String parentCommit : currentCommit.parentList) {
                if (route.containsKey(parentCommit)) {
                    break;
                } else {
                    commitQueue.add(parentCommit);
                    route.put(parentCommit, route.get(commitID) + 1);
                }

            }
        }
        return route;
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

    private static boolean checkFileInGivenCommit(File file, Commit commit) {
        if (commit.blobProjection.containsKey(file.getPath())) {
            return true;
        }
        return false;
    }

    private static boolean untrackedFileInGivenCommit(File file, Commit commit) {
        Stage addStage = Stage.getFromFile("addStage");
        if (!addStage.stageBlobMap.containsKey(file.getPath()) && !checkFileInGivenCommit(file, commit)) {
            return true;
        }
        return false;
    }
}
