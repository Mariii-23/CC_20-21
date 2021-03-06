package control;

import module.sendAndReceivedMsg.SendMSWithChangePorts;
import module.Constantes;
import module.exceptions.AckErrorException;
import module.exceptions.PackageErrorException;
import module.exceptions.TimeOutMsgException;
import module.log.Log;
import module.sendAndReceivedMsg.ControlMsgWithChangePorts;
import module.status.Information;
import module.status.SeqPedido;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.LinkedList;

public class ReceivedAndTreat implements Runnable {

  private final Information status;
  private final Log log;
  private final DatagramSocket socket;

  private final String pathDir;
  private final InetAddress clientIP;

  private final SeqPedido seqPedido;

  public ReceivedAndTreat(Information status, DatagramSocket socket, String pathDir, InetAddress clientIP,
                          SeqPedido seqPedido, Log log) {
    this.status = status;
    this.log = log;
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
      while (!status.isTerminated()) {
        try {
          socket.receive(receivedPacket);
        } catch (SocketTimeoutException e) {
          continue;
        } catch (IOException e) {
          e.printStackTrace();
          continue;
        }

        byte[] dados = receivedPacket.getData().clone();
        DatagramPacket p = new DatagramPacket(dados, dados.length, receivedPacket.getAddress(), receivedPacket.getPort());
        var msg = new ControlMsgWithChangePorts(seqPedido, clientIP, pathDir, p, status, log);
        SendMSWithChangePorts t = new SendMSWithChangePorts(msg,status);

        var n = new Thread(t);
        threads.add(n);
        n.start();
      }

      for (var t : threads) {
        t.join();
      }
    } catch (TimeOutMsgException | AckErrorException | PackageErrorException | IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
