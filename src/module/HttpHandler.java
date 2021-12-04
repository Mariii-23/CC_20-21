package module;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class HttpHandler implements  AutoCloseable , Runnable{
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
    out.println("Server: " + Constantes.CONFIG.SERVER_NAME);
    out.println("Date: "+ new Date());
    out.println("Content-type: text/html");
    out.println("Content-length: " + string.length());
    out.println("Connection: Closed" );
    out.println();
    out.println(string);
    out.flush();
  }

  private void handlePageNotFound() throws IOException {
    String string =  new Create_html_file("PageNotFound.html").createHtml("Page Not Found", "Page not found");

    out.println("HTTP/1.1 200 OK");
    out.println("Server: " + Constantes.CONFIG.SERVER_NAME);
    out.println("Date: " + new Date());
    out.println("Content-type: text/html");
    out.println("Content-length: " + string.length());
    out.println(); // blank line between headers and content, very important !
    out.println(string);
    out.flush(); // flush character output stream buffer

    out.println("HTTP/1.1 404 File Not Found");
    out.println("Server: " + Constantes.CONFIG.SERVER_NAME);
    out.println("Date: " + new Date());
    out.println("Content-type: text/html");
    out.println("Content-length: " + string.length());
    out.println(); // blank line between headers and content, very important !
    out.println(string);
    out.flush(); // flush character output stream buffer

  }

  private void handleGet(String fileRequest) throws IOException {
    switch (fileRequest) {
      case "/" : handleStatus();
      default : handlePageNotFound();
    }
  }

  private void handleNotSupported() throws  IOException {
    var name = "not_supported.html";
  }

  private void handleResponse(String method, String fileRequest) throws IOException {
    switch (method) {
      case "GET" : handleGet(fileRequest);
      default : handlePageNotFound(); // TODO METER UMA PAGINA A DIZER Q O METODO NAO EXISTE
    }
  }

  public void handle(Socket s) {

    try {
      String input = in.readLine();
      // we parse the request with a string tokenizer
      StringTokenizer parse = new StringTokenizer(input);
      String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
      // we get file requested
      var fileRequested = parse.nextToken().toLowerCase();

      handleResponse(method, fileRequested);
    } catch (IOException e) {
      //TODO MUDar
      System.out.println("ERROR: " + e);
    }
  }

  @Override
  public void run() {
    handle(s);
  }

  @Override
  public void close() throws Exception {
    out.close();
    in.close();
    s.close();
  }
}
