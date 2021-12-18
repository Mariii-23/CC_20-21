package module;

import control.ControlMsgWithChangePorts;
import control.ReceveidAndTreat;
import control.SeqPedido;
import control.SynchronizeDirectory;
import module.Exceptions.PackageErrorException;
import module.MsgType.HI;
import module.MsgType.List;

import java.io.IOException;
import java.net.*;

public class Communication implements Runnable{

  private final Information status; // server para verificar se o programa termina
  private DatagramSocket socket;

  private final String pathDir;
  private final int port;
  private final InetAddress clientIP;

  private SeqPedido seqPedido;
  //private seqPed

  public Communication(Information status,String clientIP, String pathDir) throws UnknownHostException {
    this.status = status;
    //this.serverIP = InetAddress.getByName(clientIP);
    this.clientIP = InetAddress.getByName(clientIP);
    this.port = Constantes.CONFIG.PORT_UDP;
    this.pathDir = pathDir;
    this.seqPedido = new SeqPedido();
  }

  private void connect() {
    try {
        iniciarConecao();
    } catch (SocketTimeoutException e){
      try {
        confirmarConecao();
      }catch (Exception e3){
        e3.printStackTrace();
      }
    } catch (Exception ioException) {
      ioException.printStackTrace();
    }
  }

  private void confirmarConecao() throws IOException {
    seqPedido = new SeqPedido((byte) 10);
    System.out.println("servidor");
    HI HiMSG = new HI(clientIP,port,socket,seqPedido);
    HiMSG.received();
  }

  private void iniciarConecao() throws IOException {
    socket = new DatagramSocket(port);

    // TODO ver melhor o tempo
    socket.setSoTimeout(200);

    HI hiMsg = new HI(clientIP,port,socket,seqPedido);
    try {
      hiMsg.send();
    } catch (PackageErrorException e){
      //a conecao falhou porque ele recebeu um pacote errado muitas vezes
      System.out.println("A coneção falhou");
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    connect();
    ReceveidAndTreat receveidAndTreat = new ReceveidAndTreat(status,socket,pathDir,clientIP,seqPedido);
    SynchronizeDirectory synchronizeDirectory =
        new SynchronizeDirectory(status,socket,pathDir,clientIP,port,seqPedido);
    Thread[] threads = new Thread[2];
    threads[0] = new Thread(receveidAndTreat);
    threads[1] = new Thread(synchronizeDirectory);
    threads[0].start();
    threads[1].start();
    try {
      threads[0].join();
      threads[1].join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    //status.endProgram();
    close();
  }

  public void close() {
      if(socket!=null) {
        socket.close();
      }
  }
}
