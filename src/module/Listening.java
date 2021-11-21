package module;

import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Listening implements Runnable {

  String pathDir;
  int port;

  InetAddress serverIP;
  HttpServer server;

  public Listening(String clientIP, String pathDir) throws IOException {
    //this.serverIP = InetAddress.getLocalHost();
    this.serverIP = InetAddress.getByName(clientIP);
    this.port = Constantes.CONFIG.PORT_HTTP;
    this.pathDir = pathDir;
  }

  @Override
  public void run() {
    try {
      //server = HttpServer.create(new InetSocketAddress("localhost",port),0);
      //server = HttpServer.create(new InetSocketAddress(serverIP,port),0);

      //ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
      //server.createContext("/"+serverIP,new HttpHandler(pathDir) );
      //////server.setExecutor(null);
      //server.setExecutor(threadPoolExecutor);
      //server.start();

      // nao pode ser assim, pois assim so o locahost é q recebe as cenas
      ServerSocket serverSocket = new ServerSocket(port);
      while (true) {
        Socket client = serverSocket.accept();

        InputStreamReader isr
            =  new InputStreamReader(client.getInputStream());
        BufferedReader reader = new BufferedReader(isr);
        String line = reader.readLine();
        while (!line.isEmpty()) {
          System.out.println(line);
          line = reader.readLine();
        }

        String string =  new Create_html_file(this.pathDir).createHtml();

        String httpResponse = "HTTP/1.1 200 OK\r\nContent-Length: "+string.length()+"\r\n\r\n" + string;
        OutputStream out = client.getOutputStream();
        var httpResponseBytes =   httpResponse.getBytes("UTF-8");

        out.write(httpResponseBytes);
        out.flush();
        out.close();
        client.close();
      }

      //TODO no final
      //server.stop();
    } catch (IOException e){
    }
  }

  //public static void main(String[] args) throws IOException {
  //  Listening l = new Listening("170.0.0.1","/home/mari");
  //  l.run();
  //}
}
