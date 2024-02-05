package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.writeObject;

public class Stage implements Serializable {

    private Map<String, String> pathToBlobIDMap = new HashMap<>();

    public Map<String, String> getBlobIDMap() {
        return pathToBlobIDMap;
    }

    public boolean isNewBlob(Blob blob) {
        return !pathToBlobIDMap.containsKey(blob.getBlobId());
    }

    public void deleteBlob(Blob blob) {
        pathToBlobIDMap.remove(blob.getFilePath());
    }

    public void saveRemoveStage() {
        writeObject(Repository.REMOVESTAGE_FILE, this);
    }

    public void add(Blob blob) {
        pathToBlobIDMap.put(blob.getFilePath(), blob.getBlobId());
    }

    public void saveAddStage() {
        writeObject(Repository.ADDSTAGE_FILE, this);
    }

    public Map<String, String> getPathToBlobIDMap() {
        return pathToBlobIDMap;
    }

    public List<Blob> getBlobList() {
        List<Blob> blobList = new ArrayList<>();
        for (String blobId : pathToBlobIDMap.values()) {
            Blob blob = Blob.getBlobById(blobId);
            blobList.add(blob);
        }
        return blobList;
    }

    public boolean exists(String fileName) {
        return pathToBlobIDMap.containsKey(fileName);
    }

    public Blob getBlobByPath(String path) {
        return Blob.getBlobById(getBlobIDMap().get(path));
    }

    public boolean isEmpty() {
        return getBlobIDMap().size() == 0;
    }

    public void clear() {
        getBlobIDMap().clear();
    }

    public void delete(String path) {
        pathToBlobIDMap.remove(path);
    }
}
