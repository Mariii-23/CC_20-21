package module.MsgType;

import module.Constantes;
import module.Exceptions.AckErrorException;
import module.Exceptions.PackageErrorException;
import module.Exceptions.TimeOutMsgException;
import module.MSG_interface;
import module.Type;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class ACK implements MSG_interface {

  private int port;

  InetAddress clientIP;

  Type type = Type.ACK;
  DatagramPacket packet; //
  DatagramSocket socket;

  Byte seq = (byte) 0;
  Byte seqConfirmed; // seq a confirmar

  public ACK(DatagramPacket packet,int port,DatagramSocket socket, InetAddress clientIP, byte seq) {
    this.port = port;
    this.packet = packet;
    this.clientIP = clientIP;
    this.socket = socket;
    this.seqConfirmed = getSeq(packet);
    this.seq = seq;
    this.type.flagOn();
  }


  @Override
  public int getPort() {
    return port;
  }

  @Override
  public Type getType() {
    return type;
  }

  //TODO Acrescentar o ultimo seq
  @Override
  public void createTailPacket(byte[] buff) {
    buff[2]  = seqConfirmed;
  }

  @Override
  public byte[] createMsg(byte seq){
    byte[] buff = new byte[3];
    createHeadPacket(seq,buff);
    createTailPacket(buff);
    return buff;
  }

  @Override
  public DatagramPacket createPacket(byte sed) {
    byte[] msg = createMsg(seq);
    return this.packet = new DatagramPacket(msg, msg.length, clientIP, port);
  }

  //TODO
  @Override
  public boolean validType(DatagramPacket packet) {
    var msg = packet.getData();
    return msg[1] == Type.ACK.getBytes();
  }

  private boolean valid(DatagramPacket packet) {
    var msg = packet.getData();
    System.out.println(msg[2] + "    conf " + seqConfirmed);
    return msg[2] == this.seqConfirmed;
  }

  @Override
  public void send() throws IOException {

    var packet = createPacket(seq);
    seq++;
    //System.out.println( "enviado :   "+ toString(packet));
    socket.send(packet);
  }

  @Override
  public void received() throws IOException, TimeOutMsgException, PackageErrorException, AckErrorException {

    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    DatagramPacket dpac = new DatagramPacket(buff, buff.length);
    try {
      socket.receive(dpac);
    } catch (SocketTimeoutException e){
      throw new TimeOutMsgException("Tempo de resposta ultrapassado");
    }

    if (!validType(dpac)){
      System.out.println("FOdace tipo");
      throw new PackageErrorException("Mensagem recebida nao é do tipo ack");
    }

    if (!valid(dpac)){
      System.out.println("packet mal wtf");
      throw new AckErrorException("Seq a confirmar não é o correspondido", packet.getData()[2]);
    }

    System.out.println(ACK.toString(dpac));
  }

  public String toString() {
    if (packet!=null) {
      byte[] msg = packet.getData();
      return  "SEQ: " + msg[0] + "; Type: ACK" +  "; MSG: " + msg[2];
    }
    else {
      return "Packet Invalid";
    }
  }

  public static String toString(DatagramPacket packet) {
    byte[] msg = packet.getData();
    return  "SEQ: " + msg[0] + "; Type: ACK" +  "; MSG: " + msg[2];
  }
}
