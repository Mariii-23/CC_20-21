package module.msgType;

import interfaces.MSG_interface;
import module.Constantes;
import module.exceptions.AckErrorException;
import module.exceptions.PackageErrorException;
import module.exceptions.TimeOutMsgException;
import module.log.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class ACK implements MSG_interface {

  private int port;
  private final Log log;

  private final InetAddress clientIP;

  private final Type type = Type.ACK;
  private DatagramPacket packet; //
  private DatagramSocket socket;

  private final Byte seqPedido;
  private final Byte seq = (byte) 0;
  private final Byte seqConfirmed; // seq a confirmar
  private final Byte seqSegConfirmed; // seq a confirmar
  private final Boolean isEmpty;

  public ACK(DatagramPacket packet, int port, DatagramSocket socket, InetAddress clientIP, byte seq, Log log) {
    this.log = log;
    this.port = port;
    this.packet = packet;
    this.clientIP = clientIP;
    this.socket = socket;
    if (packet == null) {
      this.seqConfirmed = (byte) 0;
      this.seqSegConfirmed = (byte) 0;
      this.isEmpty = true;
    } else {
      this.seqConfirmed = getSeq(packet); //pedido
      this.seqSegConfirmed = getSeqSegmento(packet); //segmento
      this.isEmpty = false;
    }
    this.seqPedido = seq;
    this.type.flagOn();
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setSocket(DatagramSocket socket) {
    this.socket = socket;
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public void createTailPacket(byte[] buff) {
    buff[3] = seqConfirmed;
    buff[4] = seqSegConfirmed;
  }

  //@Override
  public DatagramPacket createPacket(byte sed, byte seqSeg) {
    byte[] msg = createMsg(seqPedido, seq);
    return this.packet = new DatagramPacket(msg, msg.length, clientIP, port);
  }

  @Override
  public DatagramPacket getPacket() {
    return packet;
  }

  @Override
  public boolean validType(DatagramPacket packet) {
    var msg = packet.getData();
    return msg[0] == Type.ACK.getBytes();
  }

  private boolean valid(DatagramPacket packet) {
    var msg = packet.getData();
    return msg[4] == this.seqSegConfirmed && msg[3] == this.seqConfirmed;
  }

  @Override
  public void send() throws IOException {
    var packet = createPacket(seqPedido, seq);
    socket.send(packet);
    if (!isEmpty)
      log.addQueueSend(MSG_interface.MSGToString(packet));
  }

  @Override
  public void sendFirst(DatagramSocket socket) throws IOException {
    var packet = createPacket(seqPedido, seq);
    socket.send(packet);
    if (!isEmpty)
      log.addQueueSend(MSG_interface.MSGToString(packet));
  }

  @Override
  public void send(DatagramSocket socket) throws IOException, PackageErrorException {
    var packet = createPacket(seqPedido, seq);
    socket.send(packet);
    if (!isEmpty)
      log.addQueueSend(MSG_interface.MSGToString(packet));
  }

  @Override
  public void received() throws IOException, TimeOutMsgException, PackageErrorException, AckErrorException {
    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    DatagramPacket dpac = new DatagramPacket(buff, buff.length);
    try {
      socket.receive(dpac);
    } catch (SocketTimeoutException e) {
      throw new TimeOutMsgException("Tempo de resposta ultrapassado");
    }

    if (!validType(dpac))
      throw new PackageErrorException("Mensagem recebida nao é do tipo ack");

    if (!valid(dpac))
      throw new AckErrorException("Seq a confirmar não é o correspondido", packet.getData()[2]);

    port = dpac.getPort();
    System.out.println("RECEBI: " + ACK.toString(dpac));
  }

  public String toString() {
    if (packet != null) {
      return ACK.toString(packet);
    } else {
      return "Packet Invalid";
    }
  }

  public static String toString(DatagramPacket packet) {
    byte[] msg = packet.getData();
    return "[ACK]  -> SEQ: " + msg[1] + "; SEG: " + msg[2] + "; MSG: Seq: " + msg[3] + " Seg: " + msg[4];
  }
}
