package module.exceptions;

/**
 * Esta excepcção será lançada quando o package do tipo Ack a ser recebido é diferente do esperado.
 */
public class AckErrorException extends Exception {

  private byte receivedSeq;

  public AckErrorException() {
    super();
  }

  public AckErrorException(String message) {
    super(message);
  }

  public AckErrorException(String message, byte receivedSeq) {
    super(message);
    this.receivedSeq = receivedSeq;
  }

  public AckErrorException(byte receivedSeq) {
    super();
    this.receivedSeq = receivedSeq;
  }

  public byte getReceivedSeq() {
    return receivedSeq;
  }
}
