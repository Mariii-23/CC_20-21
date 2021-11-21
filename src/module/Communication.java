package module;

import module.MsgType.HI;

import java.io.IOException;
import java.net.*;

public class Communication {

  DatagramSocket socket;       /// nosso socket no qual enviamos coisas
  //DatagramSocket clientSocket; /// neste socket ouvimos coisas do outro sistema

  String pathDir;
  int port;

  InetAddress serverIP; // coisas que nao estao a ser bem usadas
  InetAddress clientIP; // coisas que nao estao a ser bem usadas

  byte seq;

  public Communication(String clientIP, String pathDir) throws UnknownHostException {
    this.serverIP = InetAddress.getByName(clientIP);
    this.clientIP = InetAddress.getByName(clientIP);
    this.port = Constantes.CONFIG.PORT;
    this.pathDir = pathDir;
  }

  private void connect() {

    try {
      iniciarConecao();
    } catch (IOException e){
      //try {
      //  confirmarConecao();
      //} catch (IOException e1) {
        //TODO APAGAR FRASE
        System.out.println("olha fodace");
      //}
    }
  }

  private void confirmarConecao() throws IOException {
    seq = (byte) 10;
    this.socket = new DatagramSocket(port);
    socket.setSoTimeout(2000);
    System.out.println("cliente");

    HI HiMSG = new HI(serverIP,clientIP,port,socket,seq);
    HiMSG.send();
  }

  private void iniciarConecao() throws IOException {
    socket = new DatagramSocket(port);

    seq = (byte) 0;

    try {
      HI hiMsg = new HI(serverIP,clientIP,port,socket,seq);
      hiMsg.send();
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
