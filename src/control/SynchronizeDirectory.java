package control;

import module.Information;
import module.MsgType.List;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class SynchronizeDirectory implements Runnable{
  private final Information status;
  private final DatagramSocket socket;
  //private ReentrantLock lock;

  private final String pathDir;
  private final InetAddress clientIP;
  private final int port;

  private final SeqPedido seqPedido;

  private final int time = 1000;

  public SynchronizeDirectory(Information status, DatagramSocket socket, String pathDir, InetAddress clientIP,
                              int port, SeqPedido seqPedido) {
    this.status = status;
    this.socket = socket;
    this.pathDir = pathDir;
    this.clientIP = clientIP;
    this.seqPedido = seqPedido;
    this.port = port;
  }

  private void sendList(){
    List getMsg3 = null;
    try {
      getMsg3 = new List(port, clientIP, socket, seqPedido, pathDir);
      ControlMsgWithChangePorts msg = new ControlMsgWithChangePorts(seqPedido,getMsg3,clientIP,port);
      msg.run();
    } catch (SocketException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    //System.out.println("Send first list");
    sendList();
    while (!status.isTerminated()){
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
