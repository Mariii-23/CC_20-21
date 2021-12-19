package interfaces;

import module.Constantes;
import module.exceptions.AckErrorException;
import module.exceptions.PackageErrorException;
import module.exceptions.TimeOutMsgException;
import module.log.Log;
import module.msgType.*;
import module.status.Information;
import module.status.SeqPedido;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public interface MSG_interface {
  int getPort();

  Type getType();

  void setPort(int port);

  boolean validType(DatagramPacket packet);

  String toString();

  DatagramPacket getPacket();

  void setSocket(DatagramSocket socket);

  void sendFirst(DatagramSocket socket) throws IOException;

  default byte getSeq(DatagramPacket packet) {
    return packet.getData()[1];
  }

  default byte getSeqSegmento(DatagramPacket packet) {
    return packet.getData()[2];
  }

  static byte[] getDataMsg(DatagramPacket packet) {
    byte[] received = packet.getData();
    byte[] msg = new byte[received.length];
    int i2 = 0;
    int i = Constantes.CONFIG.HEAD_SIZE;
    for (; i2 < msg.length && i < Constantes.CONFIG.BUFFER_SIZE; i++, i2++)
      msg[i2] = received[i];
    return msg;
  }

  static byte getType(DatagramPacket packet) {
    return packet.getData()[0];
  }

  static void printMSG(DatagramPacket packet) {
    System.out.println(MSGToString(packet));
  }

  static String MSGToString(DatagramPacket packet) {
    switch (getType(packet)) {
      case (byte) 0:
        return HI.toString(packet);
      case (byte) 1:
        return ACK.toString(packet);
      case (byte) 2:
        return BYE.toString(packet);
      case (byte) 4:
        return List.toString(packet);
      case (byte) 5:
        return GET.toString(packet);
      case (byte) 6:
        return SEND.toString(packet);
      default:
        return "TYPE ERROR -> byte" + (int) getType(packet);
    }
  }

  default void createHeadPacket(byte sed, byte seqSeg, byte[] buff) {
    buff[0] = getType().getBytes();
    buff[1] = sed;
    buff[2] = seqSeg;
  }

  void createTailPacket(byte[] buff);

  default public byte[] createMsg(byte seq, byte seqSeg) {
    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    createHeadPacket(seq, seqSeg, buff);
    createTailPacket(buff);
    return buff;
  }

  void send() throws IOException, PackageErrorException;

  void send(DatagramSocket socket) throws IOException, PackageErrorException;

  void received() throws IOException, TimeOutMsgException, PackageErrorException, AckErrorException;

  static MSG_interface createMsg(DatagramPacket packet, DatagramSocket socket, SeqPedido seq, String dir,
                                 Information information, Log log)
      throws IOException, TimeOutMsgException, PackageErrorException, AckErrorException {
    MSG_interface msg_interface;
    //TODO
    InetAddress clientIp = packet.getAddress();
    int port = socket.getLocalPort();
    switch (getType(packet)) {
      case (byte) 0:
        msg_interface = new HI(packet, socket.getLocalPort(), socket, seq, log);
        break;
      case (byte) 1:
        msg_interface = new ACK(packet, socket.getLocalPort(), socket, packet.getAddress(), seq.getSeq(), log);
        break;
      case (byte) 2:
        msg_interface = new BYE(packet, clientIp, port, socket, seq, information, log);
        break;
      case (byte) 4:
        msg_interface = new List(packet, socket.getLocalPort(), socket, seq, dir, log);
        break;
      case (byte) 5:
        msg_interface = new GET(packet, packet.getAddress(), socket.getLocalPort(), socket, seq, dir, log);
        break;
      case (byte) 6:
        msg_interface = new SEND(packet, packet.getAddress(), socket.getLocalPort(), socket, seq, dir, log);
        break;
      default:
        System.out.println("TYPE ERROR -> byte" + (int) getType(packet));
        return null;
    }
    return msg_interface;
  }
}
