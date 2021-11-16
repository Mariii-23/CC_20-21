package module.MsgType;

import module.Constantes;
import module.Msg;
import module.Type;

import java.net.DatagramPacket;
import java.net.InetAddress;

// tem q implementar interfac
public class HI extends Msg {

  Type type ;
  DatagramPacket packet;

  public HI(InetAddress address, int serverPort, int clientPort) {
    super(address,serverPort,clientPort);
    type = Type.Hi;
  }

  public HI(DatagramPacket packet,int serverPort,int clientPort) {
    super(packet.getAddress(), serverPort,clientPort);
    this.packet = packet;
  }

  public void createTailPacket(byte[] buff) {
    String msg = "HI";
    byte[] msgByte = msg.getBytes();

    for(int i= 2; i<msgByte.length;i++)
      buff[i] = msgByte[i-2];
  }

  public byte[] createMsg(byte sed) {
    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    createHeadPacket(sed,buff, type.getBytes());
    createTailPacket(buff);
    return buff;
  }

  public DatagramPacket createPacket(byte sed) {
    byte[] msg = createMsg(sed);

    InetAddress ipaddr = this.address;

    return this.packet = new DatagramPacket(msg, msg.length, ipaddr, clientPort);
  }

  public static boolean validType(DatagramPacket packet){
    byte[] msg = packet.getData();
    return  msg[1] == Type.Hi.getBytes();
  }

  public boolean valid(DatagramPacket packet) {
    //TODO
    return true;
  }

  public String toString() {
    if (packet!=null) {
      byte[] msg = packet.getData();
      return  "SEQ: " + msg[0] + "; Type: HI" +  "; MSG:  HI";
    }
    else {
      return "Packet Invalid";
    }
  }

  public static String toString(DatagramPacket packet1) {
    byte[] msg = packet1.getData();
    return  "SEQ: " + msg[0] + "; Type: HI" +  "; MSG:  HI";
  }
}
