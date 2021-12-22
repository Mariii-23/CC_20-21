package module.HTTP;

//import org.jetbrains.annotations.Contract;
//import org.jetbrains.annotations.NotNull;

import module.Constantes;
import module.status.Information;

import java.io.*;
import java.util.Date;
import java.util.Locale;

public class Create_html_file {
  private final File dir;
  private final String out_name;
  private final Information information;

  public Create_html_file(String name_dir , Information information) {
    this.dir = new File(name_dir);
    this.out_name = "tmp/html.html";
    this.information = information;
  }

  public Create_html_file(String name_dir, String out_name, Information information) {
    this.dir = new File(name_dir);
    this.out_name = out_name;
    this.information = information;
  }

  //@Contract(pure = true)
  private /*@NotNull*/ String html_title() {
    return "STATUS";
  }

  private /*@NotNull*/ String create_line_table(/*@NotNull*/ File file) {
    var is_file = file.isFile() ? "FILE" : "DIRECTORY";
    //var size = (double) file.length() / (1024 * 1024) ; //"mb"
    var size = file.length() / 1024;
    var date = new Date(file.lastModified());
    String equal;
    if (!information.equalFileToIgnored(file.getName()))
      equal = "TRUE";
    else equal = "FALSE";
    //var size = file.getTotalSpace(); // tem q se fazer a conversao i guess
    return "<tr><td>" + create_link_file_or_dir(file) + "</td><td>" + is_file + "</td><td>" + size +  "</td><td>" +
        date + "</td><td>" + equal + "</td></tr>\n";
  }

  private /*@NotNull*/ String create_line_tableLog(/*@NotNull*/ File file) {
    var size = file.length() / 1024;
    var date = new Date(file.lastModified());
    return "<tr><td>" + create_link_file_or_dir(file) + "</td><td>" + size +  "</td><td>" +
        date + "</td></tr>\n";
  }

  private /*@NotNull*/ String create_table() {
    var table = new StringBuilder();
    table.append("<table>\n");
    //table.append("   <caption>").append(create_link_file_or_dir(dir)).append("</caption>\n");

    table.append("<tr><th>Path</th><th>What it is?</th><th>Size (kb) </th><th>Last Modified</th><th>Synchronize?</th></tr>\n");
    var listDir = dir.listFiles();
    for (File children : listDir) {
      if (!information.logFiles.contains(children.getName()))
        table.append(create_line_table(children));
    }

    table.append("</table>\n");
    return table.toString();
  }

  private /*@NotNull*/ String create_table_logs() {
    var table = new StringBuilder();
    table.append("<table>\n");
    table.append("<tr><th>Path</th><th>Size (kb) </th><th>Last Modified</th></tr>\n");
    var listDir = dir.listFiles();
    for (File children : listDir) {
      if (information.logFiles.contains(children.getName()))
        table.append(create_line_tableLog(children));
    }

    table.append("</table>\n");
    return table.toString();
  }

  private /*@NotNull*/ String create_link_file_or_dir(/*@NotNull*/ File file) {
    String url = dir.getAbsolutePath() + "/" + file.getName();
    var link = new StringBuilder("<a href=" + "\"" + url + "\">" + file.getName() + "</a>\n");
    return link.toString();
  }

  private /*@NotNull*/ String html_body() {
    var listDir = dir.listFiles();

    var builder = new StringBuilder();
    var link_path = "<a href=" + "\"" + dir.getAbsolutePath() + "\">" + dir.getPath() + "</a>";
    builder.append("<h1>Status of directory ")
        .append(link_path)
        .append("</h1>\n");

    if (listDir != null) {

      builder.append("<h2>List of files</h2>\n")
          .append(create_table());

    } else {
      builder.append("<h2>None files</h2>\n");
    }
    return builder.toString();
  }

  private /*@NotNull*/ String html_body_logs() {
    var listDir = dir.listFiles();

    var builder = new StringBuilder();
    var link_path = "<a href=" + "\"" + dir.getAbsolutePath() + "\">" + dir.getPath() + "</a>";
    builder.append("<h1>Status")
        .append(link_path)
        .append("</h1>\n");

    if (listDir != null) {

      builder.append("<h2>List of logs files</h2>\n")
          .append(create_table_logs());

    } else {
      builder.append("<h2>None files</h2>\n");
    }
    statics(builder);
    return builder.toString();
  }

  private void statics(StringBuilder builder){
    //builder.append("<h2>Number of threads</h2>\n");
    //builder.append("<ul>\n");
    //  builder.append("<li>").append("ACTIVES       -> ").append(information.numberThreads()).append("</li>\n");
    //  builder.append("<li>").append("TOTAL CREATED -> ").append(information.numberTotalThreads()).append("</li>\n");
    //builder.append("</ul>\n");

    builder.append("<h2>STATICS</h2>\n");
    builder.append("<ul>\n");
    builder.append("<li>").append("SEND FILES -> ").append(information.numberSendFiles()).append("</li>\n");
    builder.append("<li>").append("GET FILES  -> ").append(information.numberGetFiles()).append("</li>\n");
    builder.append("</ul>\n");
  }

  public String createHtml(Boolean isLogs) throws IOException {
    BufferedReader objReader;
    try {
      objReader = new BufferedReader(new FileReader(Constantes.PATHS.PARENT_PATH + "/" + Constantes.PATHS.TEMPLATE_HTML));
    } catch (IOException e) {
      System.out.println(e.toString());
      return "";
    }

    String strCurrentLine;
    var html_builder = new StringBuilder();
    while ((strCurrentLine = objReader.readLine()) != null) {
      html_builder.append(strCurrentLine);
    }
    objReader.close();

    var htmlString = html_builder.toString();
    var title = html_title();
    String body;
    if (isLogs)
      body = html_body_logs();
    else
      body = html_body();
    htmlString = htmlString.replace("$Title", title);
    htmlString = htmlString.replace("$Body", body);
    htmlString = htmlString.replace("$Path_Style", Constantes.PATHS.STYLE_CSS);
    return htmlString.toString();
  }

  public String createHtml(String title, String body) throws IOException {
    BufferedReader objReader;
    try {
      objReader = new BufferedReader(new FileReader(Constantes.PATHS.PARENT_PATH + "/" + Constantes.PATHS.TEMPLATE_HTML));
    } catch (IOException e) {
      System.out.println(e.toString());
      return "";
    }

    String strCurrentLine;
    var html_builder = new StringBuilder();
    while ((strCurrentLine = objReader.readLine()) != null) {
      html_builder.append(strCurrentLine);
    }
    objReader.close();

    var htmlString = html_builder.toString();
    htmlString = htmlString.replace("$Title", title);
    htmlString = htmlString.replace("$Body", body);
    return htmlString.toString();
  }

  public void toHtml() throws IOException {
    var myWriter = new FileWriter(out_name);
    myWriter.write(createHtml(false));
    myWriter.close();
  }
}
