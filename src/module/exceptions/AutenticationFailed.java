package module.exceptions;

public class AutenticationFailed extends Exception{
  public AutenticationFailed() {
  }

  public AutenticationFailed(String message) {
    super(message);
  }
}
