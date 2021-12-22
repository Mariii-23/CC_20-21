package module.directory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Directory {
  private final HashMap<String, FileStruct> files;

  //public Directory(File dir) {
  //  files = new HashMap<>();

  //  for (File file : Objects.requireNonNull(dir.listFiles()))
  //    files.put(file.getName(), new FileStruct(file));
  //}

  private void addDirectory(File file , String path) {
    String actualPath;
    if (path == null || path.isEmpty())
      actualPath = file.getName();
    else actualPath = path + "/" + file.getName();
    for (var elem: Objects.requireNonNull(file.listFiles())) {
      var name =actualPath + "/" + elem.getName();
      if (elem.isDirectory()) {
        addDirectory(elem, name);
      } else {
        files.put( name, new FileStruct(name, elem.lastModified(),false));
      }
    }
  }

  public Directory(File dir) {
    files = new HashMap<>();
    for (var elem : Objects.requireNonNull(dir.listFiles())){
      if (elem.isDirectory()){
        addDirectory(elem,"");
      } else
        files.put(elem.getName(), new FileStruct(elem));
    }
  }

  public boolean addFile(FileStruct file) {
    var fail = files.putIfAbsent(file.name, file);
    return fail == null;
  }

  public boolean addFile(String name, Long lastModification, boolean isDirectory) {
    var file = new FileStruct(name, lastModification, isDirectory);
    var fail = files.putIfAbsent(file.name, file);
    return fail == null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Directory directory = (Directory) o;
    boolean success = true;
    for (FileStruct file : directory.files.values()) {
      var name = file.name;
      FileStruct ourFile = this.files.get(name);
      if (ourFile != null) {
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
    for (FileStruct fs : files.values()) {
      if (this.files.containsKey(fs.name)) {
        FileStruct aux = this.files.get(fs.name);
        if (aux.getLastModification() < fs.getLastModification()) {
          filesReceived.add(fs.name);
        }
      } else {
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
