package module.HTTP;

import module.Constantes;
import module.status.Information;

import java.io.*;
import java.util.Date;
import java.util.Objects;

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

  private String html_title() {
    return "STATUS";
  }

  private  String create_line_table( File file) {
    var is_file = file.isFile() ? "FILE" : "DIRECTORY";
    var size = file.length() / 1024;
    var date = new Date(file.lastModified());
    String equal;
    if (!information.equalFileToIgnore(file.getName()))
      equal = "TRUE";
    else equal = "FALSE";
    return "<tr><td>" + create_link_file_or_dir(file) + "</td><td>" + is_file + "</td><td>" + size +  "</td><td>" +
        date + "</td><td>" + equal + "</td></tr>\n";
  }

  private  String create_line_tableLog(File file) {
    var size = file.length() / 1024;
    var date = new Date(file.lastModified());
    return "<tr><td>" + create_link_file_or_dir(file) + "</td><td>" + size +  "</td><td>" +
        date + "</td></tr>\n";
  }

  private String create_table() {
    var table = new StringBuilder();
    table.append("<table>\n");

    table.append("<tr><th>Path</th><th>What it is?</th><th>Size (kb) </th><th>Last Modified</th><th>Synchronize?</th></tr>\n");
    var listDir = dir.listFiles();
    for (File children : Objects.requireNonNull(listDir)) {
      if (!information.logFiles.contains(children.getName()))
        table.append(create_line_table(children));
    }

    table.append("</table>\n");
    return table.toString();
  }

  private String create_table_logs() {
    var table = new StringBuilder();
    table.append("<table>\n");
    table.append("<tr><th>Path</th><th>Size (kb) </th><th>Last Modified</th></tr>\n");
    var listDir = dir.listFiles();
    for (File children : Objects.requireNonNull(listDir)) {
      if (information.logFiles.contains(children.getName()))
        table.append(create_line_tableLog(children));
    }

    table.append("</table>\n");
    return table.toString();
  }

  private String create_link_file_or_dir(File file) {
    String url = dir.getAbsolutePath() + "/" + file.getName();
    return "<a href=" + "\"" + url + "\">" + file.getName() + "</a>\n";
  }

  private String html_body() {
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

  private String html_body_logs() {
    var listDir = dir.listFiles();

    var builder = new StringBuilder();
    var link_path = "<a href=" + "\"" + dir.getAbsolutePath() + "\">" + dir.getPath() + "</a>";
    builder.append("<h1>Status")
        .append(link_path)
        .append("</h1>\n");

    if (listDir != null && listDir.length > 0) {

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
    var title = html_title();
    String body;
    if (isLogs)
      body = html_body_logs();
    else
      body = html_body();
    return createHtml(title,body, Constantes.PATHS.STYLE_CSS);
  }

  public String createHtml(String title, String body, String pathStyle) throws IOException {
    BufferedReader objReader;
    try {
      objReader = new BufferedReader(
          new FileReader(Constantes.PATHS.PARENT_PATH + "/" + Constantes.PATHS.TEMPLATE_HTML));
    } catch (FileNotFoundException e) {
      objReader = new BufferedReader(
          new FileReader(Constantes.PATHS.PARENT_PATH2 + "/" + Constantes.PATHS.TEMPLATE_HTML));
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
    htmlString = htmlString.replace("$Path_Style", pathStyle);
    return htmlString;
  }

  public void toHtml() throws IOException {
    var myWriter = new FileWriter(out_name);
    myWriter.write(createHtml(false));
    myWriter.close();
  }
}
