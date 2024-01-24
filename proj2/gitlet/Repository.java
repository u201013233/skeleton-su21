package gitlet;

import java.io.File;
import java.nio.file.Paths;

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

    /* TODO: fill in the rest of this class. */
}
