package control;

import module.log.Log;
import module.msgType.List;
import module.sendAndReceivedMsg.ControlMsgWithChangePorts;
import module.status.Information;
import module.status.SeqPedido;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class SynchronizeDirectory implements Runnable {
  private final Information status;
  private final Log log;
  private final DatagramSocket socket;
  //private ReentrantLock lock;

  private final String pathDir;
  private final InetAddress clientIP;
  private final int port;

  private final SeqPedido seqPedido;

  private final int time = 1000;

  public SynchronizeDirectory(Information status, DatagramSocket socket, String pathDir, InetAddress clientIP,
                              int port, SeqPedido seqPedido, Log log) {
    this.status = status;
    this.log = log;
    this.socket = socket;
    this.pathDir = pathDir;
    this.clientIP = clientIP;
    this.seqPedido = seqPedido;
    this.port = port;
  }

  private void sendList() {
    try {
      List getMsg3 = new List(port, clientIP, socket, seqPedido, pathDir, log);
      ControlMsgWithChangePorts msg = new ControlMsgWithChangePorts(seqPedido, getMsg3, clientIP, port, log);
      msg.run();
    } catch (SocketException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    //System.out.println("Send first list");
    sendList();
    System.out.println("Pasta sincronizada deste lado");
    status.setStartMenuOn();
    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    while (!status.isTerminated()) {
      try {
        Thread.sleep(time);
      } catch (InterruptedException e) {
        e.printStackTrace();
        continue;
      }
      sendList();
    }
  }
}
