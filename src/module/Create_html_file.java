package module;

import java.io.*;

public class Create_html_file {
  private final File dir;

  public Create_html_file(String name_dir) {
    this.dir = new File(name_dir);
  }

  private String html_title() {
    return "STATUS";
  }

  private String html_body() {
    var listFiles = dir.listFiles();

    StringBuilder builder = new StringBuilder();
    builder.append("<h1>Status of directory ")
        .append(dir.getPath())
        .append("</h1>\n");

    if (listFiles != null){
      builder.append("<h2>List of files</h2>\n")
          .append("<ol>\n");
      for (var file : listFiles)  {
        builder.append("\n<li>") .append(file.getName())
            //.append(" "+ file.getTotalSpace())
            .append("</li>\n");
      }
      builder.append("</ol>\n");
    } else {
      builder.append("<h2>None files</h2>\n");
    }

    return builder.toString();
  }

  public void toHtml()  throws IOException {
    var objReader = new BufferedReader(new FileReader(Constantes.PATHS.TEMPLATE_HTML));
    String strCurrentLine;
    StringBuilder html_builder = new StringBuilder();
    while ((strCurrentLine = objReader.readLine()) != null) {
      html_builder.append(strCurrentLine) ;
    }
    objReader.close();

    String htmlString = html_builder.toString();
    String title = html_title();
    String body = html_body();
    htmlString = htmlString.replace("$Title", title);
    htmlString = htmlString.replace("$Body", body);

    FileWriter myWriter = new FileWriter("tmp/html.html");
    myWriter.write(htmlString);
    myWriter.close();
  }
}
