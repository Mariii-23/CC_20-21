package module.Status;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Directory {
  HashMap<String,FileStruct> files;

  public Directory(File dir) {
    files = new HashMap<>();

    for(File file : Objects.requireNonNull(dir.listFiles()))
      files.put( file.getName() ,new FileStruct(file));
  }

  public Directory(List<FileStruct> files) {
    files = files;
  }

  public boolean addFile(FileStruct file) {
    var fail = files.putIfAbsent(file.name,file);
    return fail == null;
  }

  public boolean addFile(String name, Long lastModification, boolean isDirectory) {
    var file = new FileStruct(name, lastModification, isDirectory);
    var fail = files.putIfAbsent(file.name,file);
    return fail == null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Directory directory = (Directory) o;
    boolean success = true;
    for( FileStruct file :directory.files.values()){
      var name = file.name;
      FileStruct ourFile = this.files.get(name);
        if(ourFile!=null) {
          success = ourFile.equals(file);
        } else {
          success = false;
          break;
        }
    }
    return success;
  }

  @Override
  public int hashCode() {
    return Objects.hash(files);
  }
}
