package module;

import com.sun.net.httpserver.HttpServer;
import module.HTTP.HttpHandler;

import java.io.*;
import java.net.*;

public class Listening implements Runnable {

  private final Information status;
  private String pathDir;
  private int port;

  public Listening(Information status, String pathDir) throws IOException {
    this.status = status;
    this.port = Constantes.CONFIG.PORT_HTTP;
    this.pathDir = pathDir;
  }


  @Override
  public void run() {
    try {

      //TODO
      // 1.  adicionar threads

      ServerSocket serverSocket = new ServerSocket(port);
      while (!status.isTerminated()) {
        // maybe por tempo assim quando terminamos o programa ele
        // nao fica a espera de 1 ultima concecao
        Socket client = serverSocket.accept();

        try {
          Thread t = new Thread(new HttpHandler(client, pathDir));
          t.run();
          t.join();
        } catch (InterruptedException e){
          System.out.println(e.toString());
        }

        //PrintWriter out_ = new PrintWriter(client.getOutputStream());
        //BufferedReader in_ = new BufferedReader(new InputStreamReader(client.getInputStream()));

        //InputStreamReader isr
        //    =  new InputStreamReader(client.getInputStream());
        //BufferedReader reader = new BufferedReader(isr);
        //String line = reader.readLine();
        //String line = in_.readLine();
        //while (!line.isEmpty()) {
        //  System.out.println(line);
        //  line = in_.readLine();
        //}
      }

      //TODO no final
      serverSocket.close();
    } catch (IOException e){
    }
  }

  //public static void main(String[] args) throws IOException {
  //  Listening l = new Listening("170.0.0.1","/home/mari");
  //  l.run();
  //}
}
