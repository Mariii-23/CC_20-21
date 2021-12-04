import module.Communication;
import module.HTTP.Create_html_file;
import module.Information;
import module.Listening;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.Date;

public class main {

  public void try_() throws IOException {
    try (var listener = new ServerSocket(59090)) {
      System.out.println("The date server is running...");
      while (true) {
        try (var socket = listener.accept()) {
          String name = "/home/mari/uni-projetos/CC-TP2/files";
          File dir = new File(name);
          String[] children = dir.list();

          var out = new PrintWriter(socket.getOutputStream(), true);
          out.println(new Date().toString() + "\n Nao sei o q ta a acontecer");
          out.println("\n\nFILES in "+ name + "\n");
          for (String elem : children ){
            out.println(elem);
            out.println(dir.getPath() + "/" + elem);
          }
        }
      }
    }
  }

  public void create_HTML() throws IOException{
    Create_html_file html = new Create_html_file("files");
    html.toHtml();

    html = new Create_html_file("src/module","tmp/1.html");
    html.toHtml();

    html = new Create_html_file("src","tmp/2.html");
    html.toHtml();
  }


  public static void main(String[] args) throws IOException {
    String ip = args[0];
    String path = args[1];
    //TODO confirmar argumentos

    // verificar se o argumento 0 é um ip valido , verificar o formato
    // InetAddress.getByName( ... ); -> basta ver se isto nao lanca excecao

    // verificar se o argumnto 1 é uma pasta valida... isto é se o path é valido e exite

    // senoa for... terminar programa e printar o erro

    Information status = new Information();

    Communication c = new Communication(status ,ip, path);
    //TODO nao esta a dar direito
    Listening l = new Listening(status,"170.0.0.1",path);

    Thread[] t = new Thread[2];
    t[0] = new Thread(c);
    t[1] = new Thread(l);

    t[0].start();
    t[1].start();

    //t[0].join();
    //t[1].join();
  }

}
