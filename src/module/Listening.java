package module;

import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.StringTokenizer;
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

      //TODO
      // 1.  adicionar pagina de erros
      // 2.  adicionar struct que lida com pedidos ///Usar o Handle
      // 3.  adicionar threads

      ServerSocket serverSocket = new ServerSocket(port);
      while (true) {
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


        //String input = in_.readLine();
        //// we parse the request with a string tokenizer
        //StringTokenizer parse = new StringTokenizer(input);
        //String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
        //// we get file requested
        //var fileRequested = parse.nextToken().toLowerCase();
        //System.out.println(method);
        //System.out.println(fileRequested);

        //InputStreamReader isr
        //    =  new InputStreamReader(client.getInputStream());
        //BufferedReader reader = new BufferedReader(isr);
        //String line = reader.readLine();
        //String line = in_.readLine();
        //while (!line.isEmpty()) {
        //  System.out.println(line);
        //  line = in_.readLine();
        //}

        //String string =  new Create_html_file(this.pathDir).createHtml();
        //// por tudo numa linha e por /r
        //out_.println("HTTP/1.1 200 OK");
        //out_.println("Server: "+ serverIP);
        //out_.println("Date: "+ new Date());
        //out_.println("Content-type: text/html");
        //out_.println("Content-length: " + string.length());
        //out_.println("Connection: Closed" );
        //out_.println();
        //out_.println(string);
        //out_.flush();
        //out_.close();
        //in_.close();
        //client.close();

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
