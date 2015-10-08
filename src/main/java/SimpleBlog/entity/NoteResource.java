package SimpleBlog.entity;

/**
 * Created by lyoo on 10/7/2015.
 */
public class NoteResource {
    byte[] data;
    String mimeType;
    String fileName;
    String fileHashcode;
    String sourceUrl;
    int width;
    int height;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileHashcode() {
        return fileHashcode;
    }

    public void setFileHashcode(String fileHashcode) {
        this.fileHashcode = fileHashcode;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
