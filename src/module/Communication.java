package module;

import control.SeqPedido;
import module.Exceptions.AckErrorException;
import module.Exceptions.PackageErrorException;
import module.Exceptions.TimeOutMsgException;
import module.MsgType.GET;
import module.MsgType.HI;
import module.Status.FileStruct;

import java.io.File;
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
        // foi o q mandou o hi

        // receber o ls

        byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
        DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
        try {
          socket.receive(receivedPacket);
          //System.out.println("Recebi algo do ip -> " + clientIP);
          ControlMsgWithChangePorts msg = new ControlMsgWithChangePorts(seqPedido,clientIP, pathDir,receivedPacket);
          msg.run();

        } catch (IOException ex) {
          ex.printStackTrace();
        } catch (TimeOutMsgException e) {
          e.printStackTrace();
        } catch (PackageErrorException e) {
          e.printStackTrace();
        } catch (AckErrorException e) {
          e.printStackTrace();
        }
    } catch (SocketTimeoutException e){
      try {
        confirmarConecao();
        //foi o q recebeu
        // manda o ls

        ////testar mandar file
        FileStruct file = new FileStruct(new File("text"));
        GET getMsg = new GET(clientIP,port,socket,seqPedido,file,pathDir);
        ControlMsgWithChangePorts msg = new ControlMsgWithChangePorts(seqPedido,getMsg,clientIP,port);
        msg.run();

      } catch (IOException e1){
        e1.printStackTrace();
      }
    } catch (IOException ioException) {
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
    status.endProgram();
  }

  public void close() {
      if(socket!=null) {
        socket.close();
      }
  }
}
