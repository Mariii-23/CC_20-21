package module;

import module.MsgType.HI;

import java.io.IOException;
import java.net.*;

public class Communication {

  DatagramSocket socket;       /// nosso socket no qual enviamos coisas
  //DatagramSocket clientSocket; /// neste socket ouvimos coisas do outro sistema

  int serverPort;
  int clientPort;

  InetAddress serverIP; // coisas que nao estao a ser bem usadas
  InetAddress clientIP; // coisas que nao estao a ser bem usadas

  byte seq;

  public Communication(String serverIP, String clientIP) throws UnknownHostException {
    this.serverIP = InetAddress.getByName(serverIP);
    this.clientIP = InetAddress.getByName(clientIP);
  }

  private void connect() {
    try {
      iniciarConecao();
    } catch (IOException e){
      try {
        confirmarConecao();
      } catch (IOException e1) {
        //TODO APAGAR FRASE
        System.out.println("olha fodace");
      }
    }
  }

  private void confirmarConecao() throws IOException {
    clientPort = 3000;
    serverPort = 3001;
    seq = (byte) 10;
    this.socket = new DatagramSocket(serverPort);
    System.out.println("cliente");

    HI HiMSG = new HI(serverIP,clientIP,serverPort,clientPort,socket,seq);
    HiMSG.send();
  }

  private void iniciarConecao() throws IOException {
    this.serverPort = 3000;
    this.clientPort = 3001;
    socket = new DatagramSocket(serverPort);
    System.out.println("server");
      seq = (byte) 0;

    try {
      HI hiMsg = new HI(serverIP,clientIP,serverPort,clientPort,socket,seq);
      hiMsg.received();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void run() {
    connect();
  }

  public void close() {
      if(socket!=null) {
        socket.close();
      }
  }
}
