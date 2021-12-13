package module;

import module.Exceptions.AckErrorException;
import module.Exceptions.PackageErrorException;
import module.Exceptions.TimeOutMsgException;
import module.MsgType.GET;
import module.MsgType.HI;
import module.Status.FileStruct;

import java.io.File;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.locks.ReentrantLock;

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
      //GET getMsg = new GET(clientIP,port,socket, seq,pathDir);
      ////testtar receber file
      ////SEND getMsg = new SEND(clientIP,port,socket,++seq,"text2",pathDir);
      //getMsg.received();

        byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
        DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
        try {
          socket.receive(receivedPacket);

        } catch (IOException ex) {
          ex.printStackTrace();
        }
        try {
          MSG_interface.treatMsg(receivedPacket,socket,seq,pathDir);
        } catch (IOException ex) {
          ex.printStackTrace();
        } catch (TimeOutMsgException ex) {
          ex.printStackTrace();
        } catch (PackageErrorException ex) {
          ex.printStackTrace();
        } catch (AckErrorException ex) {
          ex.printStackTrace();
        }
    } catch (SocketTimeoutException e){
      try {
        confirmarConecao();
        //foi o q recebeu

        // manda o ls

        ////testar mandar file
        FileStruct file = new FileStruct(new File("text"));
        GET getMsg = new GET(clientIP,port,socket,++seq,file,pathDir);
        //FileStruct file = new FileStruct(new File("text"));
        //SEND getMsg = new SEND(clientIP,port,socket,++seq,file,pathDir);
        try {
          getMsg.send();
        } catch (PackageErrorException e2){
          e2.printStackTrace();
        } catch (Exception e1){
          e1.printStackTrace();
        }


      } catch (IOException e1){
        e1.printStackTrace();
      }
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }

  }

  private void confirmarConecao() throws IOException {
    seq = (byte) 10;
    System.out.println("servidor");
    socket.connect(clientIP,port);

    HI HiMSG = new HI(clientIP,port,socket,seq);seq++;
    HiMSG.received();
  }

  private void iniciarConecao() throws IOException {
    socket = new DatagramSocket(port);

    System.out.println(socket.getLocalPort());
    //socket.setSoTimeout(2000);
    seq = (byte) 0;

    HI hiMsg = new HI(clientIP,port,socket,seq); seq++;
    try {
      hiMsg.send();
      socket.connect(clientIP,port);
    } catch (PackageErrorException e){
      //a conecao falhou porque ele recebeu um pacote errado muitas vezes
      System.out.println("A coneção falhou");
      e.printStackTrace();
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
