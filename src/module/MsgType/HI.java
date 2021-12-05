package module.MsgType;

import module.Constantes;
import module.Exceptions.AckErrorException;
import module.Exceptions.PackageErrorException;
import module.Exceptions.TimeOutMsgException;
import module.MSG_interface;
import module.Type;

import java.io.IOException;
import java.net.*;

public class HI implements MSG_interface {

  private int port;

  //InetAddress serverIP; // coisas que nao estao a ser bem usadas
  InetAddress clientIP; // coisas que nao estao a ser bem usadas

  Type type;
  DatagramPacket packet;
  DatagramSocket socket;
  //DatagramSocket serverSocket;

  Byte seq = (byte) 0;
  Byte seqPedido;

  public HI(InetAddress clientIp, int port,DatagramSocket socket, byte seq) throws SocketException {
    //this.serverIP = serverIP;
    this.clientIP = clientIp;
    this.port = port;
    this.socket = socket;
    type = Type.Hi;
    this.packet = null;
    this.seqPedido = seq;
    //this.serverSocket = new DatagramSocket();
  }

  public HI(DatagramPacket packet,int port,DatagramSocket socket, byte seq) throws SocketException {
    this.port = port;
    this.clientIP = packet.getAddress();
    this.packet = packet;
    this.socket = socket;
    this.seqPedido = seq;
    //this.serverSocket = new DatagramSocket();
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

    for(int i= Constantes.CONFIG.HEAD_SIZE-1; i<msgByte.length;i++)
      buff[i] = msgByte[i-2];
  }

  //@Override
  public DatagramPacket createPacket(byte seq,byte seqSeg) {
    byte[] msg = createMsg(seq, seqSeg);
    return this.packet = new DatagramPacket(msg, msg.length, clientIP, port);
  }

  public boolean validType(DatagramPacket packet){
    byte[] msg = packet.getData();
    return  msg[0] == Type.Hi.getBytes();
  }


  public void send() throws IOException, SocketTimeoutException, PackageErrorException {

    var sendPackage = createPacket(seqPedido,seq);
    socket.send(sendPackage);
    seq++;

    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
    socket.setSoTimeout(2000); // prob eliminar e por isso no Communication

    boolean receveidPackage = false;
    while (!receveidPackage) {
      try {
        socket.receive(receivedPacket);
      } catch (IOException e) {
        throw new SocketTimeoutException("Não recebeu nada");
      }

      if (!validType(receivedPacket)){
        //TODO chamar controlo de fluxo
        // mais q 3 vezes e ele manda um package error
      } else {
        System.out.println("RECEBI: " + HI.toString(receivedPacket));
        ACK ack = new ACK(receivedPacket, port, socket, clientIP, seq);
        ack.send();
        receveidPackage = true;
      }
    }
  }

  public void received() throws IOException {

      boolean hiReceved = false;

      byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
      DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
      while (!hiReceved) {
        try {
          socket.receive(receivedPacket);

          hiReceved = validType(receivedPacket);
          //TODO se for falso varias vezes temos q fazer algo
          // FLUXO de congestao
          if (hiReceved) System.out.println("RECEBI: " + HI.toString(receivedPacket));

        } catch (SocketTimeoutException e) {
          // TODO fluxo de congestao
          continue;
        }
      }

      boolean hiMsgReceveid = false;
      while (!hiMsgReceveid) {
        DatagramPacket hiPacket = createPacket(seqPedido,seq);
        seq++;
        socket.send(hiPacket);

        ACK ack = new ACK(hiPacket,port,socket,clientIP,seq);
        boolean ackFail = false;
        while (!ackFail){
          try {
            ack.received();
            ackFail = true;
            hiMsgReceveid = true;
          } catch (TimeOutMsgException e){
            // TODO controlo de fluxo
            // vamos diminuindo o tempo de receber cenas
            continue;
          } catch (PackageErrorException e1) {
            // TODO controlo de fluxo
            // a partir de x pacotes errados, fechamos a conecao
            continue;
          } catch (AckErrorException e2) {
            break;
          }
        }
      }
  }

  public String toString() {
    if (packet!=null) {
      byte[] msg = packet.getData();
      return  "SEQ: " + msg[1] + " SEG: " +msg[2]  + "; Type: HI" +  "; MSG:  HI";
    }
    else {
      return "Packet Invalid";
    }
  }

  public static String toString(DatagramPacket packet) {
    byte[] msg = packet.getData();
    return  "SEQ: " + msg[1] + " SEG: " +msg[2]  + "; Type: HI" +  "; MSG:  HI";
  }
}
