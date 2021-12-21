package module.menu;

import java.util.function.Consumer;

public final class Option<T> {
  private final String command;
  private final Consumer<T> function;
  private final Boolean stop;

  public Option(String command, Consumer<T> function) {
    this.command = command;
    this.function = function;
    this.stop = false;
  }

  public Option(String command, Consumer<T> function, Boolean stop) {
    this.command = command;
    this.function = function;
    this.stop = stop;
  }

  public Boolean stop() {
    return this.stop;
  }

  public String getCommand() {
    return command;
  }

  public Consumer<T> getFunction() {
    return function;
  }
}