import control.Communication;
import module.Constantes;
import module.HTTP.Listening;
import module.log.Log;
import module.status.Information;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class main {
  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println(Constantes.PHRASES.INSTRUCTIONS);
      return;
    }
    String ip = args[0];
    try {
      InetAddress.getByName(ip);
    } catch (UnknownHostException ignored) {
      System.out.println("IP: " + ip + " is not valid\n");
      return;
    }

    String path = args[1];
    try {
      if (!Files.isDirectory(Path.of(path))) {
        System.out.println("PATH -> " + path + " dont exist or is not a directory");
        return;
      }
    } catch (InvalidPathException ignored) {
      System.out.println("PATH -> " + path + " is an invalid path");
      return;
    }

    String pathLogins = path + '/' + Constantes.PATHS.LOGINS;
    try {
      if (!Files.exists(Path.of(pathLogins))) {
        System.out.println("PATH LOGINS -> " + pathLogins + " dont exist or is not a directory\n");
        return;
      }
    } catch (InvalidPathException ignored) {
      System.out.println("PATH LOGINS -> " + pathLogins + " is an invalid path");
      return;
    }

    Information status = new Information(path);
    Log log = new Log(path, status);

    Communication c = new Communication(status, ip, path, log);
    Listening l = new Listening(status, path);

    Thread[] t = new Thread[2];
    t[0] = new Thread(c);
    t[1] = new Thread(l);

    status.increaseThread();
    status.increaseThread();
    t[0].start();
    t[1].start();

    try {
      t[1].join();
      status.decreaseThread();
      t[0].join();
      status.decreaseThread();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
