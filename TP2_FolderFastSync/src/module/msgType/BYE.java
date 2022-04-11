package module.msgType;

import interfaces.MSG_interface;
import module.Constantes;
import module.exceptions.AckErrorException;
import module.exceptions.PackageErrorException;
import module.exceptions.TimeOutMsgException;
import module.log.Log;
import module.status.Information;
import module.status.SeqPedido;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class BYE implements MSG_interface {
  private final Log log;
  private int port;
  private final InetAddress clientIP;
  private DatagramSocket socket;

  private Byte seq = (byte) 0;
  private final SeqPedido seqPedido;

  private final Type type = Type.Bye;

  private final Information information;
  private DatagramPacket packet;

  public BYE(DatagramPacket packet, InetAddress clientIP, int port, DatagramSocket socket, SeqPedido seqPedido,
             Information information, Log log) {
    this.log = log;
    this.packet = packet;
    this.clientIP = clientIP;
    this.port = port;
    this.socket = socket;
    this.seqPedido = seqPedido;
    this.information = information;
  }

  public DatagramPacket getPacket() {
    return packet;
  }

  @Override
  public void setPort(int port) {
    this.port = port;
  }

  @Override
  public void setSocket(DatagramSocket socket) {
    this.socket = socket;
  }

  @Override
  public int getPort() {
    return socket.getPort();
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public boolean validType(DatagramPacket packet) {
    var msg = packet.getData();
    return msg[0] == Type.Bye.getNum();
  }

  @Override
  public void createTailPacket(byte[] buff) {
    for (int i = Constantes.CONFIG.HEAD_SIZE; i < Constantes.CONFIG.BUFFER_SIZE; i++)
      buff[i] = 0;
  }

  public DatagramPacket createPacket() {
    byte[] msg = createMsg(seqPedido.getSeq(), seq);
    seq++;
    return new DatagramPacket(msg, msg.length, clientIP, port);
  }

  @Override
  public void send() throws IOException, PackageErrorException, TimeOutMsgException {
    this.packet = createPacket();
    socket.send(packet);
    log.addQueueSend(MSG_interface.MSGToString(packet));
    send(this.socket);
  }

  @Override
  public void sendFirst(DatagramSocket socket) throws IOException {
    packet = createPacket();
    socket.send(packet);
    log.addQueueSend(MSG_interface.MSGToString(packet));
  }

  @Override
  public void send(DatagramSocket socket) throws IOException, PackageErrorException, TimeOutMsgException {
    ACK ack = new ACK(this.packet, port, socket, clientIP, seq, log);
    boolean ackFail = false;
    int i = 0;
    while (!ackFail) {
      try {
        ack.received();
        ackFail = true;
      } catch (TimeOutMsgException | AckErrorException e) {
        if (i > 3)
          throw new TimeOutMsgException();
        packet.setPort(port);
        socket.send(packet);
        log.addQueueSend(MSG_interface.MSGToString(packet));
        i++;
      }
    }
  }

  @Override
  public void received() throws IOException, TimeOutMsgException, PackageErrorException, AckErrorException {
    information.endProgram();
  }

  public static String toString(DatagramPacket packet) {
    byte[] msg = packet.getData();
    return "[BYE]  -> SEQ: " + msg[1] + "; SEG: " + msg[2];
  }

  public String toString() {
    return toString(packet);
  }
}
