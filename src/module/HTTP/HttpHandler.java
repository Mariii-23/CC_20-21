package module.HTTP;

import module.Constantes;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;
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

  // passar para string um dado ficheiro
  private String readFile (String fileName) throws FileNotFoundException {
    File file = new File( Constantes.PATHS.PARENT_PATH , fileName);
    StringBuilder s = new StringBuilder();
    Scanner scanner = new Scanner(file);
    while( scanner.hasNextLine())
      s.append(scanner.nextLine()).append("\n");
    return s.toString();
  }

  /// Pedidos GET

  // Responder ao pedido do status GET /
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

  // Respoonder ao ficheiro de pagina not found
  private void handlePageNotFound() throws IOException {
    var string = readFile(Constantes.PATHS.PAGE_NOT_FOUND_HTML);
    out.println("HTTP/1.1 404 File Not Found");
    out.println("Server: " + Constantes.CONFIG.SERVER_NAME);
    out.println("Date: " + new Date());
    out.println("Content-type: text/html");
    out.println("Content-length: " + string.length());
    out.println();
    out.println(string);
    out.flush();
  }

  /// PEDIDOS Nao suportados

  // Responder ao pedido nao suportado
  private void handleNotSupported() throws  IOException {
    var string = readFile(Constantes.PATHS.NOT_SUPPORTED);
    out.println("HTTP/1.1 501 Not Implemented");
    out.println("Server: ");
    out.println("Date: " + new Date());
    out.println("Content-type: text/html");
    out.println("Content-length: " + string.length());
    out.println();
    out.println(string);
    out.flush();
  }

  /// HANDLERSSS

  // Handler do pedido do get
  private void handleGet(String fileRequest) throws IOException {
    switch (fileRequest) {
      case "/" : handleStatus();
      //case "/ola" : handleNotSupported();
      default : handlePageNotFound();
    }
  }

  // Handler dos pedidos
  private void handleResponse(String method, String fileRequest) throws IOException {
    switch (method) {
      case "GET" : handleGet(fileRequest);
      default : handleNotSupported();
    }
  }

  public void handle() {
    try {
      String input = in.readLine();
      StringTokenizer parse = new StringTokenizer(input);
      String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
      var fileRequested = parse.nextToken().toLowerCase();

      handleResponse(method, fileRequested);
    } catch (IOException e) {
      //TODO MUDar
      System.out.println("ERROR: " + e);
    }
  }

  @Override
  public void run() {
    handle();
  }

  @Override
  public void close() throws Exception {
    out.close();
    in.close();
    s.close();
  }
}