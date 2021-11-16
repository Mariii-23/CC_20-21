package module;

public enum Type {
  Connection_request((byte) 1), // aqui ele diria o que pretendia fazer
  Connection_reply((byte) 2),
  Connection_change((byte) 3), // mudar de porta

  Ok((byte) 4),
  Bye((byte) 5), // fim da conecçao

  Status((byte) 6), // status de tudo

  List((byte) 7), // lista dos ficheiros
  Get((byte) 8),  // da me os ficheiros x1 x2 x3
  Send((byte) 9); // envio dos ficheiros x1 x2 x3

  private final Byte num;

  Type(Byte num) {
    this.num = num;
  }

  public Byte getNum() {
    return num;
  }
}
