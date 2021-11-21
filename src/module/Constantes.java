package module;

/**
 *  Aqui serão guardadas todas as constantes por Módulos.
 */
public class Constantes {
  /**
   *  Todos os defaults paths deverão ser encontrados aqui.
   */
  public static class PATHS {
    /** Default path para o template HTML */
    public static String TEMPLATE_HTML = "files/template.html";
    /** Default path para o CSS que será usado no HTML */
    public static String STYLE_CSS = "style.css";
  }

  /**
   *  Aqui teremos as configurações básicas que serão usadas em todo o programa.
   */
  public static class CONFIG {
    /** Tamanho default do Buffer */
    public static int BUFFER_SIZE = 1024;
    /** Port default*/
    public static int PORT_UDP = 3000;
    public static int PORT_HTTP = 8080;
  }
}
