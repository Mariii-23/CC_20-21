package module;
/**
 * Aqui serão guardadas todas as constantes por Módulos.
 */
public class Constantes {
  /**
   * Todos os defaults paths deverão ser encontrados aqui.
   */
  public static class PATHS {
    /**
     * Default path para o template HTML
     */
    public static String PARENT_PATH = "../files";
    public static String PARENT_PATH2 = "files";

    public static String TEMPLATE_HTML = "template.html";
    public static String PAGE_NOT_FOUND_HTML = "page_not_found.html";
    public static String NOT_SUPPORTED = "not_supported.html";
    /**
     * Default path para o CSS que será usado no HTML
     */
    public static String STYLE_CSS = "style.css";

    public static String LOGINS = ".logins";

    public static String LOG_NAME_FILE = ".log";
    public static String LOG_Time_NAME_FILE = ".logTime";
    public static String LOG_Received_NAME_FILE = ".logReceived";

  }

  /**
   * Aqui teremos as configurações básicas que serão usadas em todo o programa.
   */
  public static class CONFIG {
    /**
     * Tamanho default do Buffer
     */
    public static int BUFFER_SIZE = 1460;
    public static int HEAD_SIZE = 3;
    public static int TAIL_SIZE = BUFFER_SIZE - HEAD_SIZE;
    /**
     * Port default
     */
    public static int PORT_UDP = 80;
    public static int PORT_HTTP = 80;

    public static String SERVER_NAME = "Grupo 6";
  }

  public static class PHRASES {
    public static String INSTRUCTIONS = "Corra o programa, fornecendo 2 arguemntos válidos\n" +
        "1º arg -> [IP do servidor a conectar]\n" +
        "2º arg -> [Caminho Completo da pasta a ser sincronizada\n" +
        "Exemplo: 10.3.3.2" + "\\" + "home" + "\\" + "core" + "\\" + "folder\n";
  }
}
