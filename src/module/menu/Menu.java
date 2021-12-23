package module.menu;


import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import static module.commom.Colors.*;

public class Menu<T> {
  private final List<Option<T>> options;
  private final String description;
  private int option;
  private Boolean stop;

  // agregacao
  public Menu(String description, List<Option<T>> options, boolean stop) {
    this.options = options;
    this.option = -1;
    this.description = description;
    this.stop = stop;
  }

  public Menu(String description, List<Option<T>> options) {
    this(description, options, false);
  }

  public String getDescription() {
    return description;
  }

  public int getOption() {
    return option;
  }

  public void setOption(int option) {
    this.option = option;
  }

  public int sizeOption() {
    return this.options.size();
  }

  public int readOption() {
    int op;
    var is = new Scanner(System.in);

    System.out.println(ANSI_BOLD + ANSI_CYAN + "Option: " + ANSI_RESET);
    try {
      op = is.nextInt();
    } catch (InputMismatchException e) { // Não foi escrito um int
      op = -1;
    }
    if (op < 0 || op > this.options.size()) {
      System.out.println("Invalid Option!!!");
      op = -1;
    }
    return op;
  }

  public Boolean isValid() {
    return this.option >= 0 && this.option < this.options.size();
  }

  public Consumer<T> getFunction() {
    return this.options.get(this.option).getFunction();
  }

  public void runMenu(T that) {
    this.showMenu();
    do {
      System.out.println(this.showMenu());
      this.setOption(this.readOption());
      System.out.println("");
      if (this.isValid()) {
        Consumer<T> function = this.getFunction();
        function.accept(that);
        if (stop() || this.stop) {
          this.option = this.sizeOption() - 1;
        }
      }
      System.out.println("");
    } while (this.getOption() != this.sizeOption() - 1);
  }

  public Boolean stop() {
    if (isValid())
      return this.options.get(option).stop();
    return false;
  }

  public String showMenu() {
    var s = new StringBuilder();
    int i = 0;
    s.append("\n");
    s.append(ANSI_BOLD + ANSI_CYAN).append("MENU").append(ANSI_RESET).append("\n");
    for (Option o : this.options) {
      s.append("\t").append(ANSI_BOLD + ANSI_CYAN).append(i).append(": ").append(ANSI_RESET);
      s.append(o.getCommand());
      s.append("\n");
      i++;
    }
    return s.toString();
  }
}
