package module.sendAndReceivedMsg;

import interfaces.MSG_interface;
import module.exceptions.AckErrorException;
import module.exceptions.AutenticationFailed;
import module.exceptions.PackageErrorException;
import module.exceptions.TimeOutMsgException;
import module.log.Log;
import module.msgType.ACK;
import module.status.Information;
import module.status.SeqPedido;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ControlMsgWithChangePorts implements Runnable {
  private final Log log;
  private final InetAddress clientIP;
  private final MSG_interface msg_interface;
  private final boolean is_send;
  private final DatagramSocket socket;
  private final int clientPort;
  private final SeqPedido seqPedido;

  public ControlMsgWithChangePorts(SeqPedido seqPedido, InetAddress clientIP, String dir, DatagramPacket packet,
                                   Information information, Log log) throws IOException,
      TimeOutMsgException, PackageErrorException, AckErrorException {
    this.log = log;
    this.seqPedido = seqPedido;
    this.is_send = false;
    this.clientIP = clientIP;
    this.socket = new DatagramSocket(0);
    this.clientPort = packet.getPort();
    this.msg_interface = MSG_interface.createMsg(packet, socket, seqPedido, dir, information, log);
    //System.out.println("Tou na porta " + socket.getLocalPort());
  }

  public ControlMsgWithChangePorts(SeqPedido seqPedido, MSG_interface msg, InetAddress clientIP, int clientPort,
                                   Log log) throws SocketException {
    this.log = log;
    this.seqPedido = seqPedido;
    this.msg_interface = msg;
    this.is_send = true;
    this.clientIP = clientIP;
    this.socket = new DatagramSocket(0);
    this.clientPort = clientPort;
    //System.out.println("Tou na porta " + socket.getLocalPort());
  }

  public void sendFirst() throws IOException {
    msg_interface.sendFirst(this.socket);
    //System.out.println("porta -> " + this.socket.getLocalPort());
  }

  @Override
  public void run() {
    if (!is_send) {
      //System.out.println("RECEBI: ");
      //System.out.println(msg_interface.toString());
      log.addQueueReceived(msg_interface.toString());

      //System.out.println("Vou mandar o ack para a porta " + clientPort);
      ACK ack;
      try {
        ack = new ACK(msg_interface.getPacket(), clientPort, socket, clientIP, seqPedido.getSeq(), log);
      } catch (Exception e) {
        e.printStackTrace();
        return;
      }
      try {
        ack.send();
      } catch (IOException e) {
        e.printStackTrace();
      }

      msg_interface.setPort(clientPort);
      msg_interface.setSocket(socket);
      try {
        msg_interface.received();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (TimeOutMsgException e) {
        //msg_interface.received();
        e.printStackTrace();
      } catch (PackageErrorException e) {
        e.printStackTrace();
      } catch (AckErrorException e) {
        e.printStackTrace();
      } catch (AutenticationFailed ignored) {
        log.status.endProgram();
        return;
      }


    } else {
      try {
        msg_interface.send(socket);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (PackageErrorException | TimeOutMsgException e) {
        e.printStackTrace();
      }
    }
    this.socket.close();
  }
}
