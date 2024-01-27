package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Repository.OBJECT_DIR;

public class Blob implements Serializable {
    private String blobId;
    private byte[] bytes;
    private File file;

    private String filePath;

    private File blobFileName;


    public Blob(File file) {
        this.file = file;
        this.bytes = readFile();
        this.filePath = file.getPath();
        this.blobId = genId();
        this.blobFileName = genBlobFileName();
    }

    private File genBlobFileName() {
        return Utils.join(OBJECT_DIR, blobId);
    }

    private String genId() {
        return Utils.sha1(filePath, bytes);
    }

    public String getBlobId() {
        return blobId;
    }

    public void setBlobId(String blobId) {
        this.blobId = blobId;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public File getBlobFileName() {
        return blobFileName;
    }

    public void setBlobFileName(File blobFileName) {
        this.blobFileName = blobFileName;
    }

    private byte[] readFile() {
        return Utils.readContents(file);
    }

    public void save() {
        Utils.writeObject(blobFileName, this);
    }

    public static Blob getBlobById(String id) {
        File file = Utils.join(OBJECT_DIR, id);
        return Utils.readObject(file, Blob.class);
    }
}
