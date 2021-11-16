package module;

import java.net.DatagramPacket;
import java.net.InetAddress;

public abstract class Msg {

  protected InetAddress address; // endereco a enviar
  protected int serverPort; // nao é preciso
  protected int clientPort;

  //private final Type type; // ou algo a dizer o byte

  public Msg(InetAddress address, int serverPort, int clientPort) {
    this.address = address;
    this.serverPort = serverPort;
    this.clientPort = clientPort;
  }

  protected void createHeadPacket(byte sed, byte[] buff, byte type) {
    buff[0] = sed;
    buff[1] = type;
  }

  public abstract void createTailPacket(byte[] buff);

  public abstract byte[] createMsg(byte sed);

  public abstract DatagramPacket createPacket(byte sed);

  //public static boolean isValid(PacketInterface packet, Type type) {


  // metodo para tratar os dados desse tipo de msg

  public abstract String toString();
}
