package module.msgType;


import control.SendMSWithChangePorts;
import interfaces.MSG_interface;
import module.Constantes;
import module.directory.Directory;
import module.directory.FileStruct;
import module.exceptions.AckErrorException;
import module.exceptions.PackageErrorException;
import module.exceptions.TimeOutMsgException;
import module.log.Log;
import module.sendAndReceivedMsg.ControlMsgWithChangePorts;
import module.status.SeqPedido;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class List implements MSG_interface {

  private int port;
  private final Log log;

  private final InetAddress clientIP;
  private final String path;

  private final Type type = Type.List;
  private final DatagramPacket packet; //
  private DatagramSocket socket;
  private final Directory dir;

  private final SeqPedido controlSeqPedido;
  private Byte seqPedido;
  private Byte seq = (byte) 0;
  private Queue<byte[]> qb;
  private Queue<DatagramPacket> packets;


  public List(int port, InetAddress clientIP, DatagramSocket socket, SeqPedido seq, String path, Log log)
      throws SocketException {
    this.log = log;
    this.port = port;
    this.clientIP = clientIP;
    this.packet = null;
    this.socket = socket;
    this.controlSeqPedido = seq;
    this.path = path;
    this.dir = new Directory(new File(path));
  }

  public List(DatagramPacket packet, int port, DatagramSocket socket, SeqPedido seq, String path,
              Log log) throws SocketException {
    this.log = log;
    this.port = port;
    this.clientIP = packet.getAddress();
    this.packet = packet;
    this.socket = socket;
    this.controlSeqPedido = seq;
    this.path = path;
    this.dir = new Directory(new File(path));
  }

  public void setSocket(DatagramSocket socket) {
    this.socket = socket;
  }

  @Override
  public DatagramPacket getPacket() {
    return packet;
  }

  public void setPort(int port) {
    this.port = port;
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public boolean validType(DatagramPacket packet) {
    var msg = packet.getData();
    return msg[0] == Type.List.getNum();
    // return true;
  }

  @Override
  public void createTailPacket(byte[] buff) {
    byte[] b = qb.remove();
    int i = Constantes.CONFIG.HEAD_SIZE;
    for (int p = 0; p < buff.length && i < Constantes.CONFIG.BUFFER_SIZE; i++, p++)
      buff[i] = b[p];
    for (; i < Constantes.CONFIG.BUFFER_SIZE; i++)
      buff[i] = (byte) 0;
  }

  public void createPackets() {
    this.seqPedido = controlSeqPedido.getSeq();
    //TODO depois verificar o que acontece quando a pasta nao tem ficheiros
    this.packets = new LinkedList<>();
    for (var i = 0; i < qb.size(); i++)
      packets.add(createPacket());
  }

  //@Override
  public DatagramPacket createPacket() {
    byte[] msg = createMsg(seqPedido, seq);
    seq++;
    return new DatagramPacket(msg, msg.length, clientIP, port);
  }

  public void putData() {
    this.qb = new LinkedList<>();
    File dir = new File(path);
    StringBuilder sb = new StringBuilder();
    for (File f : dir.listFiles()) {
      sb.append(f.getName()).append(";;").append(f.lastModified()).append(";;");
    }
    String s = sb.toString();
    byte[] b = s.getBytes(StandardCharsets.UTF_8);
    int nrP = b.length / Constantes.CONFIG.TAIL_SIZE + 1;
    for (int i = 0; i < nrP; i++) {
      qb.add(Arrays.copyOfRange(b, i * Constantes.CONFIG.TAIL_SIZE, (i + 1) * Constantes.CONFIG.TAIL_SIZE));
    }
  }

  @Override
  public void send() throws IOException, PackageErrorException {
    //TODO
    putData();
    createPackets();
    if (packets.isEmpty()) {
      ACK ack = new ACK(null, port, socket, clientIP, seqPedido, log);
      try {
        ack.send();
      } catch (Exception e) {
      }
      System.out.println("Nao mandei nenhum ficheiro");
      return;
    }

    while (!(packets.isEmpty())) {
      DatagramPacket elem = packets.remove();
      socket.send(elem);
      log.addQueueSend(MSG_interface.MSGToString(elem));
      ACK ack = new ACK(elem, port, socket, clientIP, seqPedido, log);
      seqPedido++;
      boolean ackFail = false;
      int ackError = 0;
      int packageError = 0;
      int timeOutError = 0;
      while (!ackFail) {
        try {
          ack.received();
          ackFail = true;
        } catch (TimeOutMsgException e) {
          if (timeOutError > 5)
            break;
          timeOutError++;
          // TODO controlo de fluxo
          // vamos diminuindo o tempo de receber cenas
          socket.send(elem);
          log.addQueueSend(MSG_interface.MSGToString(elem));
          //continue;
        } catch (PackageErrorException e1) {
          // TODO controlo de fluxo
          // a partir de x pacotes errados, fechamos a conecao
          if (packageError > 3) break;
          packageError++;
          socket.send(elem);
          log.addQueueSend(MSG_interface.MSGToString(elem));
          //continue;
        } catch (AckErrorException e2) {
          if (ackError > 3) break;
          ackError++;
          socket.send(elem);
          log.addQueueSend(MSG_interface.MSGToString(elem));
        }
      }
      if (ackError > 3 || packageError > 3 || timeOutError > 5) break;
    }

    ACK ack = new ACK(null, port, socket, clientIP, seqPedido, log);
    try {
      ack.send();
    } catch (Exception e) {
    }
  }

  @Override
  public void sendFirst(DatagramSocket socket) throws IOException {
    putData();
    createPackets();

    if (packets.isEmpty()) {
      System.out.println("Nao mandei nenhum ficheiro");
      //TODO Acrescentar cenas ou mudar
      // mandar um bye
      return;
    }

    DatagramPacket elem = packets.remove();
    socket.send(elem);
    log.addQueueSend(MSG_interface.MSGToString(elem));

    ACK ack = new ACK(elem, port, socket, clientIP, seqPedido, log);
    seqPedido++;
    boolean ackSuccesses = false;
    int ackError = 0;
    int packageError = 0;
    int timeOutError = 0;
    while (!ackSuccesses) {
      try {
        ack.received();
        port = ack.getPort();
        ackSuccesses = true;
      } catch (TimeOutMsgException e) {
        if (timeOutError > 5)
          break;
        timeOutError++;
        // TODO controlo de fluxo
        // vamos diminuindo o tempo de receber cenas
        socket.send(elem);
        log.addQueueSend(MSG_interface.MSGToString(elem));
        //continue;
      } catch (PackageErrorException e1) {
        // TODO controlo de fluxo
        // a partir de x pacotes errados, fechamos a conecao
        if (packageError > 3) break;
        packageError++;
        socket.send(elem);
        log.addQueueSend(MSG_interface.MSGToString(elem));
        //continue;
      } catch (AckErrorException e2) {
        if (ackError > 3) break;
        ackError++;
        socket.send(elem);
      }
    }
  }

  @Override
  public void send(DatagramSocket socket) throws IOException, PackageErrorException {
    //TODO
    sendFirst(socket);
    while (!(packets.isEmpty())) {
      DatagramPacket elem = packets.remove();
      //MSG_interface.printMSG(elem);
      elem.setPort(port);
      socket.send(elem);
      log.addQueueSend(MSG_interface.MSGToString(elem));
      ACK ack = new ACK(elem, port, socket, clientIP, seqPedido, log);
      seqPedido++;
      boolean ackFail = false;
      int ackError = 0;
      int packageError = 0;
      int timeOutError = 0;
      while (!ackFail) {
        try {
          ack.received();
          ackFail = true;
        } catch (TimeOutMsgException e) {
          if (timeOutError > 5)
            break;
          timeOutError++;
          // TODO controlo de fluxo
          // vamos diminuindo o tempo de receber cenas
          socket.send(elem);
          log.addQueueSend(MSG_interface.MSGToString(elem));
          //continue;
        } catch (PackageErrorException e1) {
          // TODO controlo de fluxo
          // a partir de x pacotes errados, fechamos a conecao
          if (packageError > 3) break;
          packageError++;
          socket.send(elem);
          log.addQueueSend(MSG_interface.MSGToString(elem));
          //continue;
        } catch (AckErrorException e2) {
          if (ackError > 3) break;
          ackError++;
          socket.send(elem);
          log.addQueueSend(MSG_interface.MSGToString(elem));
        }
      }
      if (ackError > 3 || packageError > 3 || timeOutError > 5) break;
    }

    //TODO MUDAR
    ACK ack = new ACK(null, port, socket, clientIP, seqPedido, log);
    try {
      ack.send();
    } catch (Exception e) {
    }
  }

  public String toData(byte[] msg) {
    int i;
    for (i = 0; i < msg.length && msg[i] != 0; i++) ;

    return new String(msg, 0, i, StandardCharsets.UTF_8);
  }

  public HashMap<String, FileStruct> createFileMap(String data) {
    if (data == null || data.isEmpty())
      return null;
    HashMap<String, FileStruct> hf = new HashMap<>();
    String[] meta = data.split(";;");
    for (int i = 0; i < meta.length; i = i + 2) {
      FileStruct fs = new FileStruct(meta[i], Long.valueOf(meta[i + 1]), false);
      hf.put(meta[i], fs);
    }
    return hf;
  }

  @Override
  public void received() throws IOException, TimeOutMsgException, PackageErrorException, AckErrorException {
    // recebe o pedido
    boolean fileReceved = false; // so passa a true no ultimo caso
    boolean segFileReceved = false; // so passa a true no ultimo caso

    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
    Queue<byte[]> metadata = new LinkedList<>();
    metadata.add(MSG_interface.getDataMsg(packet));

    int i = 0;
    while (!fileReceved) {
      segFileReceved = false;
      while (!segFileReceved) {
        try {
          socket.receive(receivedPacket);
          i++;
          segFileReceved = validType(receivedPacket);
          if (segFileReceved) {
            //System.out.print("RECEBI: ");
            //MSG_interface.printMSG(receivedPacket);
            log.addQueueReceived(MSG_interface.MSGToString(receivedPacket));
            ACK ack = new ACK(receivedPacket, port, socket, clientIP, controlSeqPedido.getSeq(), log);
            ack.send();
            byte[] data = MSG_interface.getDataMsg(receivedPacket);
            metadata.add(data);
          } else {
            fileReceved = true;
            break;
          }
        } catch (SocketTimeoutException e) {
        }
      }
    }
    StringBuilder sb = new StringBuilder();
    while (!metadata.isEmpty()) {
      String aux = toData(metadata.remove());
      sb.append(aux);
    }

    HashMap<String, FileStruct> hf = createFileMap(sb.toString());
    ArrayList<String> filesToReceive = dir.compareDirectories(hf);
    //if (filesToReceive != null)
    //  System.out.println("files to receive: " + filesToReceive);
    var portPrincipal = Constantes.CONFIG.PORT_UDP;
    LinkedList<Thread> threads = new LinkedList<>();
    if (filesToReceive != null)
      for (var elem : filesToReceive) {
        //if (elem.equals(Constantes.PATHS.LOG_NAME_FILE) ||
        //    elem.equals(Constantes.PATHS.LOG_Time_NAME_FILE) ||
        //    elem.equals(Constantes.PATHS.LOG_Received_NAME_FILE) ||
        //    elem.equals(Constantes.PATHS.LOGINS)
        //)
        if (log.status.equalFileToIgnored(elem))
          continue;
        FileStruct file = new FileStruct(new File(elem));
        GET getMsg = new GET(clientIP, portPrincipal, socket, controlSeqPedido, file, path, log);
        ControlMsgWithChangePorts msg = new ControlMsgWithChangePorts(controlSeqPedido, getMsg, clientIP,
            portPrincipal, log);
        SendMSWithChangePorts t = new SendMSWithChangePorts(msg);

        threads.add(new Thread(t));
        t.sendFirst();
      }

    for (var elem : threads)
      elem.start();

    for (var elem : threads)
      try {
        elem.join();
      } catch (Exception ignored) {
      }
    ;
  }

  @Override
  public String toString() {
    return toString(packet);
  }

  public static String toString(DatagramPacket packet) {
    byte[] msg = packet.getData();
    byte[] dados = MSG_interface.getDataMsg(packet);
    int i;
    for (i = 0; i < dados.length && dados[i] != (byte) 0; i++) ;

    return "[List] -> SEQ:" + msg[1] + "; SEG: " + msg[2] + "; MSG: " //Metadados";
        + new String(Arrays.copyOfRange(dados, 0, i));
  }
}