package module.MsgType;

import module.Constantes;
import module.MSG_interface;
import module.Msg;
import module.Type;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class HI implements MSG_interface {

  private int serverPort; // nao é preciso
  private int clientPort;

  InetAddress serverIP; // coisas que nao estao a ser bem usadas
  InetAddress clientIP; // coisas que nao estao a ser bem usadas

  Type type ;
  DatagramPacket packet;
  DatagramSocket socket;

  Byte seq = (byte) 0;

  public HI(InetAddress serverIP, InetAddress clientIp, int serverPort, int clientPort,DatagramSocket socket) {
    this.serverIP = serverIP;
    this.clientIP = clientIp;
    this.serverPort = serverPort;
    this.clientPort = clientPort;
    this.socket = socket;
    type = Type.Hi;
    this.packet = null;
  }

  public HI(DatagramPacket packet,int serverPort,int clientPort,DatagramSocket socket) {
    this.serverPort = serverPort;
    this.clientPort = clientPort;
    this.packet = packet;
    this.socket = socket;
  }

  @Override
  public int getServerPort() {
    return serverPort;
  }

  @Override
  public int getClientPort() {
    return clientPort;
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

  public byte[] createMsg(byte sed) {
    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    createHeadPacket(sed,buff, type.getBytes());
    createTailPacket(buff);
    return buff;
  }

  public DatagramPacket createPacket(byte sed) {
    byte[] msg = createMsg(sed);

    InetAddress ipaddr = this.serverIP;

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


  //TODO faltam os acks
  public void send() throws IOException {

    var serverSocket = new DatagramSocket();

    serverSocket.send(createPacket(seq++));

    System.out.println("mandei");

    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
    socket.receive(receivedPacket);

    System.out.println(HI.toString(receivedPacket));
  }

  //TODO faltam os acks
  public void received() throws IOException {

      var clientSocket = new DatagramSocket();
      boolean hiReceved = false;
      while (!hiReceved) {
        byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
        DatagramPacket dpac = new DatagramPacket(buff, buff.length);
        socket.receive(dpac);

        hiReceved = valid(dpac);
        System.out.println(HI.toString(dpac));

      }

      // mandar a confirmacao que recebeu
      clientSocket.send(createPacket(seq++));
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
