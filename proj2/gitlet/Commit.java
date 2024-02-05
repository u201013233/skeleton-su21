package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Repository.OBJECT_DIR;
import static gitlet.Utils.join;
import static gitlet.Utils.writeObject;

/**
 * Represents a gitlet commit object.
 * TODO: It's a good idea to give a description here of what else this Class
 * does at a high level.
 *
 * @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The message of this Commit.
     */
    private String message;

    /* TODO: fill in the rest of this class. */
    private List<String> parentIDs;
    private Date currentTime;
    private String commitID;
    private Map<String, String> pathToBlobIDMap = new HashMap<>();

    private File commitSaveFileName;

    private String timeStamp;

    public Commit() {
        this.currentTime = new Date(0);
        this.timeStamp = dateToTimeStamp(this.currentTime);
        this.message = "initial commit";
        this.parentIDs = new ArrayList<>();
        this.pathToBlobIDMap = new HashMap<>();
        this.commitID = generateID();
        this.commitSaveFileName = generateFileName();
    }

    public Commit(String message, Map<String, String> blobMap, List<String> parents) {
        this.message = message;
        this.pathToBlobIDMap = blobMap;
        this.parentIDs = parents;

        this.currentTime = new Date();
        this.timeStamp = dateToTimeStamp(this.currentTime);
        this.commitID = generateID();
        this.commitSaveFileName = generateFileName();
    }

    private File generateFileName() {
        return join(OBJECT_DIR, commitID);
    }

    private String generateID() {
        return Utils.sha1(generateTimeStamp(), message, parentIDs.toString(), pathToBlobIDMap.toString());
    }

    private String generateTimeStamp() {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.CHINA);
        return dateFormat.format(currentTime);
    }

    private static String dateToTimeStamp(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return dateFormat.format(date);
    }

    public void save() {
        writeObject(commitSaveFileName, this);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getParentIDs() {
        return parentIDs;
    }

    public void setParentIDs(List<String> parentIDs) {
        this.parentIDs = parentIDs;
    }

    public Date getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }

    public String getCommitID() {
        return commitID;
    }

    public void setCommitID(String commitID) {
        this.commitID = commitID;
    }

    public Map<String, String> getPathToBlobIDMap() {
        return pathToBlobIDMap;
    }

    public void setPathToBlobIDMap(Map<String, String> pathToBlobIDMap) {
        this.pathToBlobIDMap = pathToBlobIDMap;
    }

    public File getCommitSaveFileName() {
        return commitSaveFileName;
    }

    public void setCommitSaveFileName(File commitSaveFileName) {
        this.commitSaveFileName = commitSaveFileName;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean exists(String path) {
        return pathToBlobIDMap.containsKey(path);
    }
}
