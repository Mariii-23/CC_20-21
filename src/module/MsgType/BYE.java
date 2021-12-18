package module.MsgType;

import control.SeqPedido;
import module.Constantes;
import module.Exceptions.AckErrorException;
import module.Exceptions.PackageErrorException;
import module.Exceptions.TimeOutMsgException;
import module.Information;
import module.MSG_interface;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class BYE implements MSG_interface {

  private int port;
  private final InetAddress clientIP;
  private DatagramSocket socket;

  private Byte seq = (byte) 0;
  private final SeqPedido seqPedido;

  private final Type type = Type.Get;

  private final Information information;
  private DatagramPacket packet;

  public BYE(int port, InetAddress clientIP, DatagramSocket socket,
             SeqPedido seqPedido,DatagramPacket packet, Information information) {
    this.port = port;
    this.clientIP = clientIP;
    this.socket = socket;
    this.seqPedido = seqPedido;
    this.packet = packet;
    this.information = information;
  }

  public BYE(DatagramPacket packet, InetAddress clientIP, int port, DatagramSocket socket, SeqPedido seqPedido,
             Information information){
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
    return msg[0] == Type.ACK.getNum();
  }

  @Override
  public void createTailPacket(byte[] buff) {
    for(int i = Constantes.CONFIG.HEAD_SIZE; i < Constantes.CONFIG.BUFFER_SIZE; i++)
      buff[i] = 0;
  }

  public DatagramPacket createPacket() {
    byte[] msg = createMsg(seqPedido.getSeq(),seq); seq++;
    return new DatagramPacket(msg, msg.length, clientIP ,port);
  }

  @Override
  public void send() throws IOException, PackageErrorException {
    DatagramPacket packet = createPacket();
    socket.send(packet);
    send(this.socket);
  }

  @Override
  public void sendFirst(DatagramSocket socket) throws IOException {
    DatagramPacket packet = createPacket();
    socket.send(packet);
    this.packet = packet;
  }

  @Override
  public void send(DatagramSocket socket) throws IOException, PackageErrorException {
    ACK ack = new ACK(this.packet,port,socket,clientIP,seq);
    boolean ackFail = false;
    while (!ackFail) {
      try {
        ack.received();
        ackFail = true;
      } catch (TimeOutMsgException | AckErrorException e) {
        packet.setPort(port);
        socket.send(packet);
      } catch (PackageErrorException e1) {
        break;
      }
    }
  }

  @Override
  public void received() throws IOException, TimeOutMsgException, PackageErrorException, AckErrorException {
    information.endProgram();
  }

  public static String toString(DatagramPacket packet){
    byte[] msg = packet.getData();
    return  "[BYE]  -> SEQ: "+ msg[1] + "; SEG: " + msg[2];
  }

  public String toString() {
    return toString(packet);
  }
}
