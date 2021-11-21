package module.MsgType;

import module.Constantes;
import module.MSG_interface;
import module.Type;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
  public DatagramPacket createPacket(byte sed) {
    byte[] msg = createMsg(seq);
    return this.packet = new DatagramPacket(msg, msg.length, clientIP, port);
  }

  //TODO
  @Override
  public boolean valid(DatagramPacket packet) {
    //byte[]
    return true;
  }

  @Override
  public void send() throws IOException {
    var serverSocket = new DatagramSocket();

    var packet = createPacket(seq);
    seq++;
    serverSocket.send(packet);
    serverSocket.close();
  }

  @Override
  public void received() throws IOException {
    var clientSocket = new DatagramSocket();

    boolean received = false;
    while (!received){
      byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
      DatagramPacket dpac = new DatagramPacket(buff, buff.length);
      socket.receive(dpac);
      received = valid(dpac);
      //TODO verifcar se é o sed que ele quer
      System.out.println(ACK.toString(dpac));
      System.out.println(seqConfirmed); //apagar
    }

    clientSocket.close();
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
