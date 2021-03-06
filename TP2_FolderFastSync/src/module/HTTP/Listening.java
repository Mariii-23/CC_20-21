package module.HTTP;

import module.Constantes;
import module.status.Information;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class Listening implements Runnable {

  private final Information status;
  private final String pathDir;
  private final int port;

  public Listening(Information status, String pathDir) throws IOException {
    this.status = status;
    this.port = Constantes.CONFIG.PORT_HTTP;
    this.pathDir = pathDir;
  }

  @Override
  public void run() {
    try {
      ServerSocket serverSocket = new ServerSocket(port);
      ReentrantLock lock = new ReentrantLock();
      serverSocket.setSoTimeout(1000);
      while (!status.isTerminated()) {
        Socket client;
        try {
          client = serverSocket.accept();
        } catch (InterruptedIOException ignored) {
          continue;
        }

        try {
          //PrintWriter out_ = new PrintWriter(client.getOutputStream());
          //BufferedReader in_ = new BufferedReader(new InputStreamReader(client.getInputStream()));
          ////InputStreamReader isr
          ////    =  new InputStreamReader(client.getInputStream());
          ////BufferedReader reader = new BufferedReader(isr);
          ////String line = reader.readLine();
          //String line = in_.readLine();
          //while (!line.isEmpty()) {
          //  System.out.println(line);
          //  line = in_.readLine();
          //}
          var handler = new HttpHandler(client, pathDir, lock, status);
          handler.run();
        } catch (Exception ignored) {
        }
      }
      serverSocket.close();
    } catch (IOException ignored) {
    }
  }
}
