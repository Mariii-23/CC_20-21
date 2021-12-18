package module.Status;

import module.Pair;

import java.io.File;
import java.util.*;

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

  public ArrayList<String> compareDirectories(HashMap<String, FileStruct> files) {
    if (files == null || files.isEmpty())
      return null;
    ArrayList<String> filesReceived = new ArrayList<>();
    for (FileStruct fs: files.values()) {
      if (this.files.containsKey(fs.name)) {
        FileStruct aux = this.files.get(fs.name);
        if (aux.getLastModification() < fs.getLastModification()) {
          filesReceived.add(fs.name);
        }
      }
      else {
        filesReceived.add(fs.name);
      }

    }
    return filesReceived;
  }

  @Override
  public int hashCode() {
    return Objects.hash(files);
  }
}
