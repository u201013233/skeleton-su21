package gitlet;

import java.io.Serializable;
import java.util.HashMap;
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
}
