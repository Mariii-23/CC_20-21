package module.MsgType;

import module.Constantes;
import module.MSG_interface;
import module.Msg;
import module.Type;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class HI implements MSG_interface {

  private int port;

  //InetAddress serverIP; // coisas que nao estao a ser bem usadas
  InetAddress clientIP; // coisas que nao estao a ser bem usadas

  Type type ;
  DatagramPacket packet;
  DatagramSocket socket;

  Byte seq;

  public HI(InetAddress serverIP, InetAddress clientIp, int port,DatagramSocket socket, byte seq) {
    //this.serverIP = serverIP;
    this.clientIP = clientIp;
    this.port = port;
    this.socket = socket;
    type = Type.Hi;
    this.packet = null;
    this.seq = seq;
  }

  public HI(DatagramPacket packet,int port,DatagramSocket socket, byte seq) {
    this.port = port;
    this.packet = packet;
    this.socket = socket;
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

  public void createTailPacket(byte[] buff) {
    String msg = "HI";
    byte[] msgByte = msg.getBytes();

    for(int i= 2; i<msgByte.length;i++)
      buff[i] = msgByte[i-2];
  }

  public DatagramPacket createPacket(byte seq) {
    byte[] msg = createMsg(seq);
    return this.packet = new DatagramPacket(msg, msg.length, clientIP, port);
  }

  public static boolean validType(DatagramPacket packet){
    byte[] msg = packet.getData();
    return  msg[1] == Type.Hi.getBytes();
  }

  public boolean valid(DatagramPacket packet) {
    //TODO
    return true;
  }


  public void send() throws IOException {

    var serverSocket = new DatagramSocket();

    serverSocket.send(createPacket(seq));
    seq++;

    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
    try {
      socket.receive(receivedPacket);
      System.out.println(HI.toString(receivedPacket));

      // mandar a confirmacao que recebeu
      receivedPacket = createPacket(seq);
      seq++;
      serverSocket.send(receivedPacket);
    } catch (IOException e){
      System.out.println("Ainda nao esta ninguem a escuta");
      received();
    }


    ACK ack = new ACK(receivedPacket,port,socket,clientIP,seq);
    ack.received();
  }

  public void received() throws IOException {

      var clientSocket = new DatagramSocket();

      clientSocket.send(createPacket(seq));
      seq++;

      boolean hiReceved = false;
      byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
      DatagramPacket receivedPacket = new DatagramPacket(buff, buff.length);
    while (!hiReceved) {
        receivedPacket = new DatagramPacket(buff, buff.length);
        try {
          socket.receive(receivedPacket);
        } catch (SocketTimeoutException e){
          continue;
        }

        hiReceved = valid(receivedPacket);
        System.out.println(HI.toString(receivedPacket));
      }

      ACK ack = new ACK(receivedPacket,port,socket,clientIP,seq);
      ack.send();
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

  public static String toString(DatagramPacket packet) {
    byte[] msg = packet.getData();
    return  "SEQ: " + msg[0] + "; Type: HI" +  "; MSG:  HI";
  }
}
