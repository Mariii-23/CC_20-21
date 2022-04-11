package control;

import module.Constantes;
import module.exceptions.AutenticationFailed;
import module.exceptions.PackageErrorException;
import module.log.Log;
import module.msgType.HI;
import module.status.Information;
import module.status.SeqPedido;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Communication implements Runnable {

  private final Information status;
  private final Log log;
  private DatagramSocket socket;

  private final String pathDir;
  private final int port;
  private final InetAddress clientIP;

  private SeqPedido seqPedido;

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
    System.out.println("Listening...");
    HI HiMSG = new HI(clientIP, port, socket, seqPedido, log);
    HiMSG.received();
  }

  private void iniciarConecao() throws IOException, AutenticationFailed {
    socket = new DatagramSocket(port);
    socket.setSoTimeout(200);

    HI hiMsg = new HI(clientIP, port, socket, seqPedido, log);
    try {
      hiMsg.send();
    } catch (PackageErrorException e) {
      // a conecao falhou porque ele recebeu um pacote errado muitas vezes
      System.out.println("A conexão falhou");
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

    ReceivedAndTreat receivedAndTreat = new ReceivedAndTreat(status, socket, pathDir, clientIP, seqPedido, log);
    SynchronizeDirectory synchronizeDirectory =
        new SynchronizeDirectory(status, socket, pathDir, clientIP, port, seqPedido, log);
    RunMenu menu = new RunMenu(status, port, clientIP, log, seqPedido);
    Thread[] threads = new Thread[4];
    threads[0] = new Thread(receivedAndTreat);
    threads[1] = new Thread(synchronizeDirectory);
    threads[2] = new Thread(log);
    threads[3] = new Thread(menu);
    threads[3].start();
    status.increaseThread();
    threads[2].start();
    status.increaseThread();
    threads[0].start();
    status.increaseThread();
    threads[1].start();
    status.increaseThread();
    try {
      for (var thread : threads) {
        thread.join();
        status.decreaseThread();
      }
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
