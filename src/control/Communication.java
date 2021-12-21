package control;

import module.Constantes;
import module.exceptions.AutenticationFailed;
import module.exceptions.PackageErrorException;
import module.log.Log;
import module.msgType.HI;
import module.sendAndReceivedMsg.ReceveidAndTreat;
import module.status.Information;
import module.status.SeqPedido;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Communication implements Runnable {

  private final Information status; // server para verificar se o programa termina
  private final Log log;
  private DatagramSocket socket;

  private final String pathDir;
  private final int port;
  private final InetAddress clientIP;

  private SeqPedido seqPedido;
  //private seqPed

  public Communication(Information status, String clientIP, String pathDir, Log log) throws UnknownHostException {
    this.status = status;
    this.log = log;
    this.clientIP = InetAddress.getByName(clientIP);
    this.port = Constantes.CONFIG.PORT_UDP;
    this.pathDir = pathDir;
    this.seqPedido = new SeqPedido();
  }

  private void connect() throws AutenticationFailed {
    try {
      iniciarConecao();
    } catch (SocketTimeoutException e) {
      try {
        confirmarConecao();
      } catch (AutenticationFailed ignored) {
        throw new AutenticationFailed();
      } catch (Exception e3) {
        e3.printStackTrace();
      }
    } catch (AutenticationFailed ignored) {
      throw new AutenticationFailed();
    } catch (Exception ioException) {
      ioException.printStackTrace();
    }
  }

  private void confirmarConecao() throws IOException, AutenticationFailed {
    seqPedido = new SeqPedido((byte) 10);
    System.out.println("servidor");
    HI HiMSG = new HI(clientIP, port, socket, seqPedido, log);
    HiMSG.received();
  }

  private void iniciarConecao() throws IOException, AutenticationFailed {
    socket = new DatagramSocket(port);

    // TODO ver melhor o tempo
    socket.setSoTimeout(200);

    HI hiMsg = new HI(clientIP, port, socket, seqPedido, log);
    try {
      hiMsg.send();
    } catch (PackageErrorException e) {
      //a conecao falhou porque ele recebeu um pacote errado muitas vezes
      System.out.println("A coneção falhou");
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    try {
      connect();
    } catch (AutenticationFailed ignored) {
      System.out.println("A Autenticação falhou\nA terminar ligação");
      close();
      status.endProgram();
      return;
    }

    ReceveidAndTreat receveidAndTreat = new ReceveidAndTreat(status, socket, pathDir, clientIP, seqPedido, log);
    SynchronizeDirectory synchronizeDirectory =
        new SynchronizeDirectory(status, socket, pathDir, clientIP, port, seqPedido, log);
    RunMenu menu = new RunMenu(status);
    Thread[] threads = new Thread[4];
    threads[0] = new Thread(receveidAndTreat);
    threads[1] = new Thread(synchronizeDirectory);
    threads[2] = new Thread(log);
    threads[3] = new Thread(menu);
    threads[3].start();
    threads[2].start();
    threads[0].start();
    threads[1].start();
    try {
      for (var thread : threads)
        thread.join();
      //threads[0].join();
      //threads[1].join();
      //threads[2].join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    status.endProgram();
    close();
  }

  public void close() {
    if (socket != null) {
      socket.close();
    }
  }
}
