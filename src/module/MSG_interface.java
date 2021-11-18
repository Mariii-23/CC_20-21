package module;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public interface MSG_interface {
  //InetAddress getAddress(); // endereco a enviar
  int getServerPort(); // nao é preciso
  int getClientPort();
  Type getType();



  public boolean valid(DatagramPacket packet);

  public String toString();

  //temos que implementar
  //public static boolean validType(DatagramPacket packet) {
  //  return false;
  //}

  default byte getSeq(DatagramPacket packet) {
    return packet.getData()[0];
  }

  default void createHeadPacket(byte sed, byte[] buff) {
    buff[0] = sed;
    buff[1] = getType().getBytes();
  }

  public void createTailPacket(byte[] buff);

  default public byte[] createMsg(byte seq){
    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    createHeadPacket(seq,buff);
    createTailPacket(buff);
    return buff;
  }

  public DatagramPacket createPacket(byte seq);

  public void send() throws IOException;
  public void received() throws IOException;
}
