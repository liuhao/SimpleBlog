package simpleblog.entity;

/** Note resource Created by lyoo on 10/7/2015. */
public class NoteResource {
  private String data;
  private String mimeType;
  private String fileName;
  private String fileHashcode;
  private String sourceUrl;
  private String width;
  private String height;

  public String getData() {
    return data;
  }

  public void setData(String data) {
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

  public String getWidth() {
    return width;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public String getHeight() {
    return height;
  }

  public void setHeight(String height) {
    this.height = height;
  }
}
