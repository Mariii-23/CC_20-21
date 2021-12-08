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

  Byte seqPedido;
  Byte seq = (byte) 0;
  Byte seqConfirmed; // seq a confirmar
  Byte seqSegConfirmed; // seq a confirmar

  public ACK(DatagramPacket packet,int port,DatagramSocket socket, InetAddress clientIP, byte seq) {
    this.port = port;
    this.packet = packet;
    this.clientIP = clientIP;
    this.socket = socket;
    if (packet==null){
      this.seqConfirmed = (byte) 0;
      this.seqSegConfirmed = (byte) 0;
    }else{
      this.seqConfirmed = getSeq(packet); //pedido
      this.seqSegConfirmed = getSeqSegmento(packet); //segmento
    }
    this.seqPedido = seq;
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
    buff[3]  = seqConfirmed;
    buff[4]  = seqSegConfirmed;
  }

  //@Override
  //public byte[] createMsg(byte seq, byte seqSeg){
  //  byte[] buff = new byte[5];
  //  createHeadPacket(seqPedido,seq, buff);
  //  createTailPacket(buff);
  //  return buff;
  //}

  //@Override
  public DatagramPacket createPacket(byte sed, byte seqSeg) {
    byte[] msg = createMsg(seqPedido, seq);
    return this.packet = new DatagramPacket(msg, msg.length, clientIP, port);
  }

  //TODO
  @Override
  public boolean validType(DatagramPacket packet) {
    var msg = packet.getData();
    return msg[0] == Type.ACK.getBytes();
  }

  private boolean valid(DatagramPacket packet) {
    var msg = packet.getData();
    //System.out.println(msg[4] + "    seq segmento " + seqSegConfirmed);
    //System.out.println(msg[3] + "    pedido " + seqConfirmed);
    return msg[4] == this.seqSegConfirmed && msg[3] == this.seqConfirmed;
  }

  @Override
  public void send() throws IOException {
    var packet = createPacket(seqPedido,seq);
    seq++;
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
      throw new PackageErrorException("Mensagem recebida nao é do tipo ack");
    }

    if (!valid(dpac)){
      throw new AckErrorException("Seq a confirmar não é o correspondido", packet.getData()[2]);
    }

    System.out.println("RECEBI: "+ ACK.toString(dpac));
  }

  public String toString() {
    if (packet!=null) {
      return ACK.toString(packet);
    }
    else {
      return "Packet Invalid";
    }
  }

  public static String toString(DatagramPacket packet) {
    byte[] msg = packet.getData();
    return  "[ACK]  -> SEQ: " + msg[1] + "; SEG: " +msg[2] + "; Type: ACK" + "; MSG: " + msg[3] + " | "+ msg[4];
  }
}
