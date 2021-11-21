package module.Exceptions;

/**
 * Esta excepcção será lançada quando o package do tipo Ack a ser recebido é diferente do esperado.
 */
public class AckErrorException extends Exception {
  public AckErrorException() {
    super();
  }

  public AckErrorException(String message) {
    super(message);
  }
}
