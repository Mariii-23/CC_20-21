package module;

import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class HttpHandler implements  AutoCloseable{
  private final String pathDir;
  private final PrintWriter out;
  private final BufferedReader in;
  private final Socket s;

  public HttpHandler(Socket s,String dirName) throws IOException{
    this.pathDir = dirName;
    this.s = s;
    out = new PrintWriter(s.getOutputStream());
    in = new BufferedReader(new InputStreamReader(s.getInputStream()));
  }

  private void handleStatus() throws IOException{
    String string =  new Create_html_file(pathDir).createHtml();
    out.println("HTTP/1.1 200 OK");
    out.println("Server: ola");
    out.println("Date: "+ new Date());
    out.println("Content-type: text/html");
    out.println("Content-length: " + string.length());
    out.println("Connection: Closed" );
    out.println();
    out.println(string);
    out.flush();
    out.close();
  }

  private void handleNF() throws IOException {
    String string =  new Create_html_file("/home/mari/books").createHtml();
    out.println("HTTP/1.1 400 OK");
    out.println("Server: ola");
    out.println("Date: "+ new Date());
    out.println("Content-type: text/html");
    out.println("Content-length: " + string.length());
    out.println("Connection: Closed" );
    out.println();
    out.println(string);
    out.flush();
    out.close();
  }

  private void handleGet(String path) {

  }

  //private void handleResponse(String method, String path) throws IOException {
  //  switch (method){
  //    case "GET"  => handleGet(path),
  //
  //  }
  //}

  public void handle(Socket s) throws IOException {
    String line = in.readLine();
    int i=0;
    StringTokenizer parse = null;
    String method = null;
    String fileRequested = null;
    while (!line.isEmpty()) {
        if (i==0) {
          parse = new StringTokenizer(line);
          method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
          // we get file requested
          fileRequested = parse.nextToken().toLowerCase();
        }
        System.out.println(line);
        line = in.readLine();
    }
  }

  @Override
  public void close() throws Exception {
    out.close();
    in.close();
    s.close();
  }
}
