import control.Communication;
import module.Constantes;
import module.HTTP.Listening;
import module.log.Log;
import module.status.Information;

import java.io.File;
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
      InetAddress.getByName( ip );
    } catch (UnknownHostException ignored) {
      System.out.println("IP: "+ip+ " is not valid\n");
      return;
    }

    String path = args[1];
    try {
      if (!Files.isDirectory(Path.of(path))) {
        System.out.println("PATH -> "+path+" dont exist or is not a directory");
        return;
      }
    } catch (InvalidPathException ignored){
        System.out.println("PATH -> "+path+" is an invalid path");
        return;
    }

    Information status = new Information();
    Log log = new Log(path + '/' + Constantes.CONFIG.LOG_NAME_FILE, status);

    Communication c = new Communication(status, ip, path, log);
    Listening l = new Listening(status, path);

    Thread[] t = new Thread[2];
    t[0] = new Thread(c);
    t[1] = new Thread(l);

    t[0].start();
    t[1].start();

    try {
      t[0].join();
      t[1].join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
