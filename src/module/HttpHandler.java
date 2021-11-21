package module;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class HttpHandler implements com.sun.net.httpserver.HttpHandler {

  private String dirName;

  public HttpHandler(String dirName) {
    this.dirName = dirName;
  }

  private void handleResponse(HttpExchange httpExchange) throws IOException {
    OutputStream out = httpExchange.getResponseBody();
    String string =  new Create_html_file(dirName).createHtml();

    httpExchange.sendResponseHeaders(200,string.length());
    out.write(string.getBytes());
    out.flush();
    out.close();
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    System.out.println("Recebi uma conecao");
    handleResponse(exchange);
  }

}
