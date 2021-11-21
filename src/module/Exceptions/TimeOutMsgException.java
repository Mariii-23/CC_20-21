package module.Exceptions;

/**
 * Esta excepção será lançada no caso de ser definido um dado tempo para a receção de uma dada msg,
 * e essa mesma não ser recebida antes do tempo previsto.
 */
public class TimeOutMsgException extends Exception{
  public TimeOutMsgException() {
    super();
  }

  public TimeOutMsgException(String message) {
    super(message);
  }
}
