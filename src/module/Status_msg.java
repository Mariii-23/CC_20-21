package module;

public enum Status_msg {
  Only((byte) 0),
  First((byte) 1),
  Middle((byte) 2),
  Last((byte) 3);

  private final Byte num;

  Status_msg(Byte num) {
    this.num = num;
  }

  public Byte getNum() {
    return num;
  }
}
