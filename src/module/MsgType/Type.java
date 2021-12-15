package module.MsgType;

import module.MsgType.HI;

import java.net.DatagramPacket;

public enum Type {
  Hi((byte) 0), // 64 ultimo
  //Connection_change((byte) 3), // mudar de porta

  ACK((byte) 1),
  Bye((byte) 2), // fim da conecçao

  Status((byte) 3), // status de tudo

  List((byte) 4), // lista dos ficheiros
  Get((byte) 5),  // da me o ficheiros
  Send((byte) 6); // envio do ficheiro

  //private final Byte num_max = (byte) 64;
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

  public static byte seeType(byte num) {
    if (num < (byte) 64)
      return num;
    else {
      return  (byte) (((int) num) - 64);
    }
  }

  Type(Byte num, boolean flag) {
      this.num = num;
      this.flagLast = flag;
  }

  public Byte getNum() {
    return num;
  }

  public Byte getBytes() {
    //if (flagLast)
    //  return (byte) (num.intValue() + 64);
    //else
      return num;
  }

  public Boolean getFlagLast() {
    return flagLast;
  }

  public void flagOn(){
    this.flagLast = true;
  }

  public void flagOff(){
    this.flagLast = false;
  }
}
