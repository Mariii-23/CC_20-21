package module;

import control.ControlMsgWithChangePorts;
import control.SeqPedido;
import module.Exceptions.AckErrorException;
import module.Exceptions.PackageErrorException;
import module.Exceptions.TimeOutMsgException;
import module.MsgType.GET;
import module.MsgType.HI;
import module.MsgType.List;
import module.Status.FileStruct;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.LinkedList;

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
      //List getMsg = new List(port, clientIP, socket, seqPedido, pathDir);
      //testtar receber file
      //SEND getMsg = new SEND(clientIP,port,socket,++seq,"text2",pathDir);
      //getMsg.received();

      // meu
        byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
        DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
        try {
          int i =0;
          //Thread thread[] = new Thread[2];
          LinkedList<Thread> thread = new LinkedList<>();
          while ( true){
            try {
              socket.receive(receivedPacket);
              //System.out.println("RECEBI:");
              //MSG_interface.printMSG(receivedPacket);
            } catch (SocketTimeoutException e) {
              //e.printStackTrace();
              continue;
            }

            //System.out.println("recebi e vou fazer o received");
            byte[] dados = receivedPacket.getData().clone();
            DatagramPacket p = new DatagramPacket(dados,dados.length,receivedPacket.getAddress(),receivedPacket.getPort());
            var msg = new ControlMsgWithChangePorts(seqPedido,clientIP, pathDir,p);
            System.out.println("EU sei q recebi isto algo no principal");
            //MSG_interface.printMSG(p);
            //System.out.println("Received");
            SendMSGwithChangePorts t = new SendMSGwithChangePorts(msg);

            var n = new Thread(t);
            thread.add(n);
            n.start();
            //thread[i] = new Thread(t);
            //thread[i].start();
            //i++;
          }

          //TODO mudar o true pra outra cena
          //for (var elem : thread)
          //elem.join();

          //thread[0].join();
          //thread[1].join();

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

 //////////////// Mandar 1 file
        //FileStruct file = new FileStruct(new File("text"));
        //GET getMsg = new GET(clientIP,port,socket,seqPedido,file,pathDir);
        //ControlMsgWithChangePorts msg = new ControlMsgWithChangePorts(seqPedido,getMsg,clientIP,port);
        //msg.run();
////////////////////

        /////////// JORGE ////////////////
        List getMsg3 = new List(port, clientIP, socket, seqPedido, pathDir);
        ControlMsgWithChangePorts msg = new ControlMsgWithChangePorts(seqPedido,getMsg3,clientIP,port);
        try {
          //getMsg3.send();
          System.out.println("Vou mandar o list");
          msg.run();
        } catch (Exception e1){
          System.out.println("HEHEHHEHE");
          e1.printStackTrace();
        }

        /////////////////////////////
        byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
        DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
        try {
          int i =0;
          //Thread thread[] = new Thread[2];
          LinkedList<Thread> thread = new LinkedList<>();
          while ( true){
            try {
              socket.receive(receivedPacket);
              //System.out.println("RECEBI:");
              //MSG_interface.printMSG(receivedPacket);
            } catch (SocketTimeoutException ignored) {
              //e.printStackTrace();
              continue;
            }

            //System.out.println("recebi e vou fazer o received");
            byte[] dados = receivedPacket.getData().clone();
            DatagramPacket p = new DatagramPacket(dados,dados.length,receivedPacket.getAddress(),receivedPacket.getPort());
            var msg1 = new ControlMsgWithChangePorts(seqPedido,clientIP, pathDir,p);
            System.out.println("EU sei q recebi isto algo no principal");
            //MSG_interface.printMSG(p);
            //System.out.println("Received");
            SendMSGwithChangePorts t = new SendMSGwithChangePorts(msg1);

            var n = new Thread(t);
            thread.add(n);
            n.start();
            //thread[i] = new Thread(t);
            //thread[i].start();
            //i++;
          }

          //TODO mudar o true pra outra cena
          //for (var elem : thread)
          //elem.join();

          //thread[0].join();
          //thread[1].join();

        } catch (IOException ex) {
          ex.printStackTrace();
        } catch (TimeOutMsgException e1) {
          e1.printStackTrace();
        } catch (PackageErrorException e1) {
          e1.printStackTrace();
        } catch (AckErrorException e1) {
          e1.printStackTrace();
        }

        //  FileStruct file = new FileStruct(new File("ola"));
      //  GET getMsg = new GET(clientIP,port,socket,seqPedido,file,pathDir);
      //  ControlMsgWithChangePorts msg = new ControlMsgWithChangePorts(seqPedido,getMsg,clientIP,port);

      //  FileStruct file1 = new FileStruct(new File("text"));
      //  GET getMsg1 = new GET(clientIP,port,socket,seqPedido,file1,pathDir);
      //  ControlMsgWithChangePorts msg1 = new ControlMsgWithChangePorts(seqPedido,getMsg1,clientIP,port);
      //  SendMSGwithChangePorts t1 = new SendMSGwithChangePorts(msg);
      //  SendMSGwithChangePorts t2 = new SendMSGwithChangePorts(msg1);

      //  ////testar mandar file
      //    Thread t[] = new Thread[2];
      //      t[0] = new Thread(t1);
      //      t[1] = new Thread(t2);
      //      t1.sendFirst();
      //      t2.sendFirst();
      //      t[0].start();
      //      t[1].start();

      //      t[0].join();
      //} catch (IOException | InterruptedException | PackageErrorException | TimeOutMsgException e1){
      //  e1.printStackTrace();
      //}
      }catch (Exception e3){
        e3.printStackTrace();
      }

    } catch (IOException ioException) {
      ioException.printStackTrace();
    } catch (Exception e1) {
      e1.printStackTrace();
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
