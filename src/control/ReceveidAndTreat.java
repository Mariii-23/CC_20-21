package control;

import module.Constantes;
import module.Exceptions.AckErrorException;
import module.Exceptions.PackageErrorException;
import module.Exceptions.TimeOutMsgException;
import module.Information;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.LinkedList;

public class ReceveidAndTreat implements Runnable {

  private final Information status;
  private final DatagramSocket socket;
  //private ReentrantLock lock;

  private final String pathDir;
  private final InetAddress clientIP;

  private final SeqPedido seqPedido;

  public ReceveidAndTreat(Information status, DatagramSocket socket, String pathDir, InetAddress clientIP, SeqPedido seqPedido) {
    this.status = status;
    this.socket = socket;
    this.pathDir = pathDir;
    this.clientIP = clientIP;
    this.seqPedido = seqPedido;
  }

  @Override
  public void run() {
    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
    try {
      LinkedList<Thread> threads = new LinkedList<>();
      while ( !status.isTerminated() ){
        try {
          socket.receive(receivedPacket);
        } catch (SocketTimeoutException e) {
          continue;
        } catch (IOException e) {
          e.printStackTrace();
          continue;
        }

        byte[] dados = receivedPacket.getData().clone();
        DatagramPacket p = new DatagramPacket(dados,dados.length,receivedPacket.getAddress(),receivedPacket.getPort());
        var msg = new ControlMsgWithChangePorts(seqPedido,clientIP, pathDir,p, status);
        //System.out.println("EU sei q recebi isto algo no principal");
        SendMSGwithChangePorts t = new SendMSGwithChangePorts(msg);

        var n = new Thread(t);
        threads.add(n);
        n.start();
      }

      for(var t : threads)
        t.join();

    } catch (TimeOutMsgException | AckErrorException | PackageErrorException | IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
