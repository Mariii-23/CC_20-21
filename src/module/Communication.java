package module;

import module.Exceptions.AckErrorException;
import module.Exceptions.PackageErrorException;
import module.Exceptions.TimeOutMsgException;
import module.MsgType.HI;
import module.MsgType.SEND;
import module.Status.FileStruct;

import java.io.File;

import java.io.IOException;
import java.net.*;

public class Communication implements Runnable{

  private Information status; // server para verificar se o programa termina

  private DatagramSocket socket;

  private String pathDir;
  private int port;

  private InetAddress clientIP; // coisas que nao estao a ser bem usadas

  private byte seq;

  public Communication(Information status,String clientIP, String pathDir) throws UnknownHostException {
    this.status = status;
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
      SEND getMsg = new SEND(clientIP,port,socket, seq,pathDir);
      //testtar receber file
      //GET getMsg = new GET(clientIP,port,socket,++seq,"text2",pathDir);
        getMsg.received();
      System.out.println("Supostamente mandei o file");

    } catch (SocketTimeoutException e){
      try {
        confirmarConecao();
        //foi o q recebeu

        // manda o ls

        ////testar mandar file
        FileStruct file = new FileStruct(new File("text"));
        SEND getMsg = new SEND(clientIP,port,socket,++seq,file,pathDir);
        //FileStruct file = new FileStruct(new File("text"));
        //GET getMsg = new GET(clientIP,port,socket,++seq,file,pathDir);
        try {
          getMsg.send();
          System.out.println("Supostamente recebi o file");

        } catch (PackageErrorException e2){
          System.out.println(e2.toString());
        } catch (Exception e1){
          System.out.println("HEHEHHEHE");
        }

      } catch (IOException e1){
        System.out.println("olha fodace: \n"+e1);
      }
    } catch (IOException | TimeOutMsgException | PackageErrorException | AckErrorException ioException) {
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
    status.endProgram();
  }

  public void close() {
      if(socket!=null) {
        socket.close();
      }
  }


}
