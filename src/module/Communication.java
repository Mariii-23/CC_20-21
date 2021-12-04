package module;

import module.Exceptions.PackageErrorException;
import module.MsgType.HI;
import java.io.File;

import java.io.IOException;
import java.net.*;

public class Communication implements Runnable{

  DatagramSocket socket;       /// nosso socket no qual enviamos coisas
  //DatagramSocket clientSocket; /// neste socket ouvimos coisas do outro sistema

  String pathDir;
  int port;

  //InetAddress serverIP; // coisas que nao estao a ser bem usadas
  InetAddress clientIP; // coisas que nao estao a ser bem usadas

  byte seq;

  public Communication(String clientIP, String pathDir) throws UnknownHostException {
    //this.serverIP = InetAddress.getByName(clientIP);
    this.clientIP = InetAddress.getByName(clientIP);
    this.port = Constantes.CONFIG.PORT_UDP;
    this.pathDir = pathDir;
  }

  private void connect() {

    try {
        iniciarConecao();
        // foi o q mandou o hi

        // receber o ls

    } catch (SocketTimeoutException e){
      try {
        confirmarConecao();
        //foi o q recebeu

        // manda o ls

      } catch (IOException e1){
        System.out.println("olha fodace");
      }
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }
  }

  private void confirmarConecao() throws IOException {
    seq = (byte) 10;
    //this.socket = new DatagramSocket(port);
    System.out.println("servidor");

    HI HiMSG = new HI(clientIP,port,socket,seq);
    HiMSG.received();
  }

  private void iniciarConecao() throws IOException {
    socket = new DatagramSocket(port);
    //socket.setSoTimeout(2000);

    seq = (byte) 0;

    HI hiMsg = new HI(clientIP,port,socket,seq);

    try {
      hiMsg.send();
    } catch (PackageErrorException e){
      //a conecao falhou porque ele recebeu um pacote errado muitas vezes
      System.out.println("A coneção falhou");
      return;
    }
  }


  @Override
  public void run() {
    connect();
  }

  public void close() {
      if(socket!=null) {
        socket.close();
      }
  }


}
