package module.Exceptions;

/**
 * Esta excepção deverá ser lançada quando o package encontra-se ou mal formatado,
 * campos inválidos ou o seu tipo é diferente do esperado.
 */
public class PackageErrorException extends Exception {
  public PackageErrorException() {
  }

  public PackageErrorException(String message) {
    super(message);
  }
}
