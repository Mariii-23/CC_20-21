package module.Status;

import java.io.File;

public class FileStruct {
  protected String name;
  protected Long lastModification;

  public FileStruct(File file) {
    this.name = file.getPath();
    this.lastModification = file.lastModified();
  }
}
