package module;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public interface MSG_interface {
  //InetAddress getAddress(); // endereco a enviar
  int getServerPort(); // nao é preciso
  int getClientPort();
  Type getType();


  public void createTailPacket(byte[] buff);

  public boolean valid(DatagramPacket packet);

  public String toString();

  //temos que implementar
  //public static boolean validType(DatagramPacket packet) {
  //  return false;
  //}

  default void createHeadPacket(byte sed, byte[] buff, byte type) {
    buff[0] = sed;
    buff[1] = getType().getBytes();
  }

  public void send() throws IOException;
  public void received() throws IOException;
}
