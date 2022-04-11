package module.msgType;

public enum Type {
  Hi((byte) 0), // 64 ultimo

  ACK((byte) 1),
  Bye((byte) 2), // fim da conecçao
  List((byte) 4), // lista dos ficheiros
  Get((byte) 5),  // da me o ficheiros
  Send((byte) 6); // envio do ficheiro

  private final Byte num; //numero do tipo
  private boolean flagLast = false;

  Type(Byte num) {
    if (num < (byte) 64)
      this.num = num;
    else {
      this.num = (byte) (num.intValue() - 64);
      this.flagLast = true;
    }
  }

  public Byte getNum() {
    return num;
  }

  public Byte getBytes() {
    return num;
  }

  public void flagOn() {
    this.flagLast = true;
  }
}
