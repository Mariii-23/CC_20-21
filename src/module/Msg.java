package module;

public abstract class Msg {


  // guardar os bytes da msg


  //private final Type type;


  // criar msg
  // sed | Type | Status_msg | MSG
  public abstract Byte[] create_msg();

  // metodo  para enviar msgs

  // metodo para tratar os dados desse tipo de msg
}
