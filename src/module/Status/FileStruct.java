package module.Status;

import java.io.File;
import java.util.Objects;

public class FileStruct {
  protected String name;
  protected Long lastModification;
  protected boolean isDirectory;

  public FileStruct(File file) {
    this.name = file.getPath();
    this.lastModification = file.lastModified();
    this.isDirectory = file.isDirectory();
  }

  public FileStruct(String name, Long lastModification, boolean isDirectory) {
    this.name = name;
    this.lastModification = lastModification;
    this.isDirectory = isDirectory;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FileStruct that = (FileStruct) o;
    return isDirectory == that.isDirectory &&
        name.equals(that.name) &&
        lastModification.equals(that.lastModification);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, lastModification, isDirectory);
  }
}
