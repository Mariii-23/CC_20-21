package module.menu;

import module.log.Log;
import module.msgType.BYE;
import module.sendAndReceivedMsg.ControlMsgWithChangePorts;
import module.sendAndReceivedMsg.SendMSWithChangePorts;
import module.status.Information;
import module.status.SeqPedido;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class MenuView {
  private final Information information;
  private final int port;
  private final InetAddress clientIp;
  private final Log log;
  private final SeqPedido seqPedido;

  public MenuView(Information information, int port, InetAddress clientIp, Log log, SeqPedido seqPedido){
    this.information = information;
    this.port = port;
    this.clientIp = clientIp;
    this.log = log;
    this.seqPedido = seqPedido;
  }

  private static String readString() {
    var s = new Scanner(System.in);
    return s.nextLine();
  }

  private static int readInt() throws InputMismatchException {
    var s = new Scanner(System.in);
    return s.nextInt();
  }

  public void viewAddFileToIgnored() {
    try {
      System.out.println("File name: ");
      var name = readString();
      information.addFileToIgnored(name);
    } catch (Exception e) {
      System.out.println("Nome do ficheiro dado errado");
    }
  }

  public void viewAddFileToSynchronize() {
    try {
      System.out.println("File name: ");
      var name = readString();
      information.addFileToSynchronize(name);
    } catch (Exception e) {
      System.out.println("Nome do ficheiro dado errado");
    }
  }

  public void viewShowFilesToIgnored() {
    System.out.println(information.filesToIgnoredToString());
  }

  public void viewTerminatedProgram(){
    sendBye();
    information.endProgram();
  }

  private void sendBye() {
    BYE bye = new BYE(null, clientIp, port, null , seqPedido, information ,log);
    ControlMsgWithChangePorts msg = null;
    try {
      msg = new ControlMsgWithChangePorts(seqPedido, bye, clientIp,
          port, log);
    } catch (SocketException e) {
      e.printStackTrace();
    }
    SendMSWithChangePorts t = new SendMSWithChangePorts(msg, information);
    try {
      t.sendFirst();
    } catch (IOException e) {
      e.printStackTrace();
    }
    t.run();
  }

  public void startMenu() {
    information.startMenu();
  }
}
