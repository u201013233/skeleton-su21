package gitlet;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.*;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 * TODO: It's a good idea to give a description here of what else this Class
 * does at a high level.
 *
 * @author TODO
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
    public static final File OBJECT_DIR = join(GITLET_DIR, "objects");
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    public static final File ADDSTAGE_FILE = join(GITLET_DIR, "add_stage");
    public static final File REMOVESTAGE_FILE = join(GITLET_DIR, "remove_stage");
    public static Commit currCommit;
    public static Stage addStage = new Stage();
    public static Stage removeStage = new Stage();

    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        Utils.mkdir(GITLET_DIR);
        Utils.mkdir(OBJECT_DIR);
        Utils.mkdir(REFS_DIR);
        Utils.mkdir(HEADS_DIR);

        initCommit();
        initHEAD();
        initHeads();
    }

    private static void initHeads() {
        File header = join(HEADS_DIR, "master");
        writeObject(header, currCommit.getCommitID());
    }

    private static void initHEAD() {
        writeObject(HEAD_FILE, "master");
    }

    private static void initCommit() {
        Commit initCommit = new Commit();
        currCommit = initCommit;
        initCommit.save();
    }

    public static void checkIfInitialized() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    public static void add(String path) {
        File file = getFileFromCwd(path);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Blob blob = new Blob(file);
        storeBlob(blob);
    }

    private static void storeBlob(Blob blob) {
        currCommit = readCurrentCommit();
        addStage = readAddStage();
        removeStage = readRemoveStage();

        // commit 不含有blob
        if (!currCommit.getPathToBlobIDMap().containsValue(blob.getBlobId())) {
            if (addStage.isNewBlob(blob)) {
                if (removeStage.isNewBlob(blob)) {
                    blob.save();
                    addStage.add(blob);
                    addStage.saveAddStage();
                } else {
                    removeStage.deleteBlob(blob);
                    removeStage.saveRemoveStage();
                }
            }
        }

    }

    private static Stage readRemoveStage() {
        if (!REMOVESTAGE_FILE.exists()) {
            return new Stage();
        }
        return readObject(REMOVESTAGE_FILE, Stage.class);
    }

    private static Stage readAddStage() {
        if (!ADDSTAGE_FILE.exists()) {
            return new Stage();
        }
        return readObject(ADDSTAGE_FILE, Stage.class);
    }

    private static Commit readCurrentCommit() {
        String currCommmitID = readCurrCommmitID();
        File headerFile = join(OBJECT_DIR, currCommmitID);
        return readObject(headerFile, Commit.class);
    }

    private static String readCurrCommmitID() {
        String currBranch = readCurrBranch();
        File headerFile = join(HEADS_DIR, currBranch);
        return readContentsAsString(headerFile);
    }

    private static String readCurrBranch() {
        return readContentsAsString(HEAD_FILE);
    }

    private static File getFileFromCwd(String path) {
        return Paths.get(path).isAbsolute() ? new File(path) : join(CWD, path);
    }

    public static void commit(String message) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit newCommit = newCommit(message);
        saveNewCommit(newCommit);
    }

    private static void saveNewCommit(Commit newCommit) {
        newCommit.save();
        addStage.clear();
        addStage.saveAddStage();

        removeStage.clear();
        removeStage.saveRemoveStage();

        saveHeads(newCommit);
    }

    private static void saveHeads(Commit newCommit) {
        currCommit = newCommit;
        String currBranch = readCurrBranch();
        File headerFile = join(HEADS_DIR, currBranch);
        writeContents(headerFile, newCommit.getCommitID());
    }

    private static Commit newCommit(String message) {
        // 从暂存区中获取所有文件
        Map<String, String> addBlobMap = findAddBlobMap();
        Map<String, String> removeBlobMap = findRemoveBlobMap();
        checkIfNewCommit(addBlobMap, removeBlobMap);

        currCommit = readCurrentCommit();
        Map<String, String> pathToBlobIDMap = currCommit.getPathToBlobIDMap();
        Map<String, String> blobMap = caculateBlobMap(pathToBlobIDMap, addBlobMap, removeBlobMap);
        List<String> parents = findParents();
        return new Commit(message, blobMap, parents);
    }

    private static List<String> findParents() {
        List<String> parents = new ArrayList<>();
        currCommit = readCurrentCommit();
        parents.add(currCommit.getCommitID());
        return parents;
    }

    private static Map<String, String> caculateBlobMap(Map<String, String> pathToBlobIDMap,
                                                       Map<String, String> addBlobMap,
                                                       Map<String, String> removeBlobMap) {
        if (!addBlobMap.isEmpty()) {
            for (String path : addBlobMap.keySet()) {
                pathToBlobIDMap.put(path, addBlobMap.get(path));
            }
        }
        if (!removeBlobMap.isEmpty()) {
            for (String path : removeBlobMap.keySet()) {
                pathToBlobIDMap.remove(path);
            }
        }

        return pathToBlobIDMap;
    }

    private static Map<String, String> findRemoveBlobMap() {
        Map<String, String> removeBlobMap = new HashMap<>();
        removeStage = readRemoveStage();
        List<Blob> removeBlobList = removeStage.getBlobList();
        for (Blob b : removeBlobList) {
            removeBlobMap.put(b.getFilePath(), b.getBlobId());
        }
        return removeBlobMap;
    }

    private static void checkIfNewCommit(Map<String, String> addBlobMap,
                                         Map<String, String> removeBlobMap) {
        if (addBlobMap.isEmpty() && removeBlobMap.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
    }


    private static Map<String, String> findAddBlobMap() {
        addStage = readAddStage();
        List<Blob> blobList = addStage.getBlobList();
        Map<String, String> addBlobMap = new HashMap<>();
        for (Blob blob : blobList) {
            addBlobMap.put(blob.getFilePath(), blob.getBlobId());
        }
        return addBlobMap;
    }

    public static void log() {
        currCommit = readCurrentCommit();
        while (currCommit != null && !currCommit.getParentIDs().isEmpty()) {
            if (isMergeCommit(currCommit)) {
                printMergeCommit(currCommit);
            } else {
                printCommit(currCommit);
            }
            List<String> partents = currCommit.getParentIDs();
            currCommit = readCommitById(partents.get(0));
        }
    }

    private static Commit readCommitById(String commitId) {
        if (commitId.length() == 40) {
            File file = join(OBJECT_DIR, commitId);
            if (!file.exists()) {
                return null;
            }
            return Utils.readObject(file, Commit.class);
        } else {
            List<String> objectID = plainFilenamesIn(OBJECT_DIR);
            for (String id : objectID) {
                if (id.startsWith(commitId)) {
                    return Utils.readObject(join(OBJECT_DIR, id), Commit.class);
                }
            }
            return null;
        }
    }

    private static void printCommit(Commit currCommit) {
        System.out.println("===");
        printCommitID(currCommit);
        printCommitDate(currCommit);
        printCommitMessage(currCommit);
    }

    private static void printCommitMessage(Commit currCommit) {
        System.out.println(currCommit.getMessage() + "\n");
    }

    private static void printCommitDate(Commit currCommit) {
        System.out.println("commit " + currCommit.getTimeStamp());
    }

    private static void printCommitID(Commit currCommit) {
        System.out.println("commit " + currCommit.getCommitID());
    }

    private static void printMergeCommit(Commit currCommit) {
        System.out.println("===");
        printCommitID(currCommit);
        printMergeMark(currCommit);
        printCommitDate(currCommit);
        printCommitMessage(currCommit);
    }

    private static void printMergeMark(Commit currCommit) {
        List<String> parentsCommitID = currCommit.getParentIDs();
        String parent1 = parentsCommitID.get(0);
        String parent2 = parentsCommitID.get(1);
        System.out.println("Merge: " + parent1.substring(0, 7) + " " + parent2.substring(0, 7));
    }

    private static boolean isMergeCommit(Commit currCommit) {
        return currCommit.getParentIDs().size() > 1;
    }

    public static void rm(String fileName) {
        File file = getFileFromCwd(fileName);
        String path = file.getPath();
        addStage = readAddStage();
        currCommit = readCurrentCommit();

        if (addStage.exists(path)) {
            addStage.delete(path);
            addStage.saveAddStage();
        } else if (currCommit.exists(path)) {
            removeStage = readRemoveStage();
            String blobId = currCommit.getPathToBlobIDMap().get(path);
            Blob bolb = getBolbById(blobId);
            removeStage.add(bolb);
            removeStage.saveRemoveStage();
            deleteFile(file);
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    private static Blob getBolbById(String blobId) {
        File blobFile = join(OBJECT_DIR, blobId);
        return readObject(blobFile, Blob.class);
    }

    public static void globalLog() {
        List<String> list = plainFilenamesIn(OBJECT_DIR);
        for (String id : list) {
            Commit commit = readCommitById(id);
            if (isMergeCommit(commit)) {
                printMergeCommit(commit);
            } else {
                printCommit(commit);
            }
        }
    }

    private static void printID(List<String> idList) {
        if (idList.isEmpty()) {
            System.out.println("Found no commit with that message.");
        } else {
            for (String id : idList) {
                System.out.println(id);
            }
        }
    }

    public static void find(String findMessage) {
        List<String> commitList = plainFilenamesIn(OBJECT_DIR);
        List<String> resultList = new ArrayList<>();
        for (String commitId : commitList) {
            Commit commit = readCommitById(commitId);
            if (findMessage.equals(commit.getMessage())) {
                resultList.add(commitId);
            }
        }
        printID(resultList);
    }

    public static void status() {
        printBranches();
        printStagedFile();
        printRemovedFiles();
        printModifiedNotStagedFile();
        printUntrackedFiles();
    }

    private static void printUntrackedFiles() {
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    private static void printModifiedNotStagedFile() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
    }

    private static void printRemovedFiles() {
        System.out.println("=== Removed Files ===");
        removeStage = readRemoveStage();
        for (Blob blob : removeStage.getBlobList()) {
            System.out.println(blob.getBlobFileName());
        }
        System.out.println();
    }

    private static void printStagedFile() {
        System.out.println("=== Staged Files ===");
        addStage = readAddStage();
        for (Blob blob : addStage.getBlobList()) {
            System.out.println(blob.getBlobFileName());
        }
        System.out.println();
    }

    private static void printBranches() {
        List<String> list = plainFilenamesIn(HEADS_DIR);
        String currBranch = readCurrBranch();
        System.out.println("=== Branches ===");
        System.out.println("*" + currBranch);

        if (list != null && list.size() > 1) {
            for (String branch : list) {
                if (!currBranch.equals(branch)) {
                    System.out.println(branch);
                }
            }
        }
    }

    public static void branch(String branchName) {
        checkIfNewBranch(branchName);
        addNewBranchToHeads(branchName);
    }

    private static void addNewBranchToHeads(String branchName) {
        File branchFile = join(HEADS_DIR, branchName);
        currCommit = readCurrentCommit();
        writeContents(branchFile, currCommit.getCommitID());
    }

    private static void checkIfNewBranch(String branchName) {
        List<String> list = plainFilenamesIn(HEADS_DIR);
        if (list != null && list.size() > 0) {
            for (String s : list) {
                if (s.equals(branchName)) {
                    System.out.println("A branch with that name already exists.");
                    System.exit(0);
                }
            }
        }
    }

    public void checkout(String fileName) {
        Commit currentCommit = readCurrentCommit();
        List<String> fileNames = currentCommit.getFileNames();
        if  (fileNames.contains(fileName)) {
            Blob blob = currentCommit.getBlobByFileName(fileName);
            writeBlobToCWD(blob);
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    private void writeBlobToCWD(Blob blob) {
        byte[] content = blob.getBytes();
        File file = join(CWD, blob.getFile().getName());
        writeContents(file, content);
    }
}
