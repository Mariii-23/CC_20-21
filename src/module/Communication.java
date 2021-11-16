package module;

import module.MsgType.HI;

import java.io.IOException;
import java.net.*;

public class Communication {

  DatagramSocket socket;       /// nosso socket no qual enviamos coisas
  DatagramSocket clientSocket; /// neste socket ouvimos coisas do outro sistema

  int serverPort;
  int clientPort;

  InetAddress serverIP; // coisas que nao estao a ser bem usadas
  InetAddress clientIP; // coisas que nao estao a ser bem usadas

  byte seq;

  public Communication(String serverIP, String clientIP) throws UnknownHostException {
    this.serverIP = InetAddress.getByName(serverIP);
    this.clientIP = InetAddress.getByName(clientIP);
  }

  public void connect2() {
    try {
      iniciarConecao();
    } catch (IOException e){
      try {
        confirmarConecao();
      } catch (IOException e1) {
        System.out.println("olha fodace");
      }
    }
  }

  public void confirmarConecao() throws IOException {
    clientPort = 3000;
    serverPort = 3001;
    seq = (byte) 10;
    this.socket = new DatagramSocket();
    this.clientSocket = new DatagramSocket(serverPort);
    System.out.println("cliente");

    DatagramPacket hiPacket = new HI(clientIP,serverPort,clientPort).createPacket(seq++);
    //TODO tem q tar dentro de um while e o receive tem q ter um tempo
    socket.send(hiPacket);
    //System.out.println(hiPacket.toString());

    // receber a confirmacao do server
    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
    clientSocket.receive(receivedPacket);

    System.out.println(HI.toString(receivedPacket));

    // mandar ack
  }

  public void iniciarConecao() throws IOException {
      DatagramSocket datasoc = new DatagramSocket(3000);
      System.out.println("server");
      this.serverPort = 3000;
      this.clientPort = 3001;
      seq = (byte) 0;

      HI hiMsg = new HI(clientIP,serverPort,clientPort);

      boolean hiReceved = false;
      while (!hiReceved) {
        byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
        DatagramPacket dpac = new DatagramPacket(buff, buff.length);
        datasoc.receive(dpac);

        hiReceved = hiMsg.valid(dpac);
        System.out.println(HI.toString(dpac));

      }

      // mandar a confirmacao que recebeu
      datasoc.send(hiMsg.createPacket(seq++));

      // receber ack

  }

  public void close() {
      //serverSocket.close();
      if(socket!=null) {
        socket.close();
      }
  }
}
