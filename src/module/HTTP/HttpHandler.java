package module.HTTP;

import module.Constantes;
import module.status.Information;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;

public class HttpHandler implements Runnable {
  private final String pathDir;
  private final PrintWriter out;
  private final BufferedReader in;
  private final Socket s;
  private final ReentrantLock l;
  private final Information information;

  public HttpHandler(Socket s, String dirName, ReentrantLock l, Information information) throws IOException {
    this.pathDir = dirName;
    this.s = s;
    out = new PrintWriter(s.getOutputStream());
    in = new BufferedReader(new InputStreamReader(s.getInputStream()));
    this.l = l;
    this.information = information;
  }

  /**
   * Le e passa um dado ficheiro para string
   * @param fileName nome do ficheiro
   * @return String
   * @throws FileNotFoundException
   */
  private String readFile(String fileName) throws FileNotFoundException {
    File file ;
      if (Files.exists(Path.of(Constantes.PATHS.PARENT_PATH + "/" + fileName)))
        file = new File(Constantes.PATHS.PARENT_PATH, fileName);
      else
        file = new File(Constantes.PATHS.PARENT_PATH2, fileName);

    StringBuilder s = new StringBuilder();
    Scanner scanner = new Scanner(file);
    while (scanner.hasNextLine())
      s.append(scanner.nextLine()).append("\n");
    return s.toString();
  }

  /** Pedidos SEND */

  // Responder ao pedido do status SEND e GET /

  /**
   * Handler do pedido get and send
   * @param isLog Boolean que serve para determinar que html deverá ser entregue
   * @throws IOException Lança exceção caso algo falhe
   */
  private void handleStatus(Boolean isLog) throws IOException {
    String string = new Create_html_file(pathDir, information).createHtml(isLog);
    out.println("HTTP/1.1 200 OK");
    out.println("Server: " + Constantes.CONFIG.SERVER_NAME);
    out.println("Date: " + new Date());
    out.println("Content-type: text/html");
    out.println("Content-length: " + string.length());
    out.println("Connection: Closed");
    out.println();
    out.println(string);
    out.flush();
  }

  /**
   * Handler que responde a um pedido de PAG NOT FOUND
   * @throws IOException Lança exceção caso algo falhe
   */
  private void handlePageNotFound() throws FileNotFoundException {
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

  /** PEDIDOS Nao suportados */

  /**
   * Handler que responde a um pedido não suportado
   * @throws FileNotFoundException Lança exceção caso algo falhe
   */
  private void handleNotSupported() throws FileNotFoundException {
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

  //* HANDLERS /

  /**
   * Handler do pedido get
   * @param fileRequest
   * @throws IOException
   */
  private void handleGet(String fileRequest) throws IOException {
    switch (fileRequest) {
      case "/":
        handleStatus(false);
        break;
      case "/log":
        handleStatus(true);
        break;
      default:
        handlePageNotFound();
    }
  }

  /**
   *  Handler dos pedidos
   * @param method
   * @param fileRequest
   * @throws IOException
   */
  private void handleResponse(String method, String fileRequest) throws IOException {
    switch (method) {
      case "SEND":
        //handleGet(fileRequest);
        //break;
      case "GET":
        handleGet(fileRequest);
        break;
      default:
        handleNotSupported();
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
      System.out.println("ERROR: " + e);
    }
  }

  @Override
  public void run() {
    try {
      l.lock();
      handle();
    } finally {
      l.unlock();
    }
  }
}
