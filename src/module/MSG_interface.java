package module;

import module.Exceptions.AckErrorException;
import module.Exceptions.PackageErrorException;
import module.Exceptions.TimeOutMsgException;
import module.MsgType.ACK;
import module.MsgType.SEND;
import module.MsgType.HI;
import module.MsgType.GET;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public interface MSG_interface {
  int getPort();
  Type getType();

  public boolean validType(DatagramPacket packet);
  public String toString();

  default byte getSeq(DatagramPacket packet) {
    return packet.getData()[1];
  }

  default byte getSeqSegmento(DatagramPacket packet) {
    return packet.getData()[2];
  }

  static byte[] getDataMsg(DatagramPacket packet){
    byte[] received =  packet.getData();
    byte[] msg = new byte[received.length];
    int i2=0;
    int i = Constantes.CONFIG.HEAD_SIZE - 1;
    for (; i2 < msg.length && i<Constantes.CONFIG.BUFFER_SIZE ; i++,i2++ )
      msg[i2] = received[i];
    return msg;
  }

  static byte getType(DatagramPacket packet){
    return packet.getData()[0];
  }

  static void printMSG(DatagramPacket packet){
    switch (getType(packet)) {
      case (byte) 0:
        System.out.println(HI.toString(packet)); break;
      case (byte) 1 :
        System.out.println(ACK.toString(packet)); break;
      case (byte) 5:
        System.out.println(GET.toString(packet)); break;
      case (byte) 6:
        System.out.println(SEND.toString(packet)); break;
      default:
        System.out.println("TYPE ERROR -> byte" + (int) getType(packet));
    }
  }

  default void createHeadPacket(byte sed, byte sedSeg, byte[] buff) {
    buff[0] = getType().getBytes();
    buff[1] = sed;
    buff[2] = sedSeg;
  }

  public void createTailPacket(byte[] buff);

  default public byte[] createMsg(byte seq, byte seqSeg){
    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    createHeadPacket(seq, seqSeg, buff);
    createTailPacket(buff);
    return buff;
  }

  public void send() throws IOException, PackageErrorException;
  public void received() throws IOException, TimeOutMsgException, PackageErrorException, AckErrorException;

  static void treatMsg(DatagramPacket packet, DatagramSocket socket,byte seq,String dir) throws IOException, TimeOutMsgException, PackageErrorException, AckErrorException {
    MSG_interface msg_interface;
    switch (getType(packet)) {
      case (byte) 0:
        msg_interface = new HI(packet,socket.getLocalPort(),socket, seq);
        break;
      case (byte) 1:
        msg_interface = new ACK(packet,socket.getLocalPort(),socket,socket.getInetAddress(),seq);
        break;
      case (byte) 5:
        msg_interface = new GET(packet,socket.getInetAddress(),socket.getLocalPort(),socket,seq,dir);
        break;
      case (byte) 6:
        msg_interface = new SEND(packet,socket.getInetAddress(),socket.getLocalPort(),socket,seq,dir);
        break;
      default:
        System.out.println("TYPE ERROR -> byte" + (int) getType(packet));
        return;
    }
    msg_interface.received();
  }
}
