package module.msgType;

import interfaces.MSG_interface;
import module.Constantes;
import module.exceptions.AckErrorException;
import module.exceptions.PackageErrorException;
import module.exceptions.TimeOutMsgException;
import module.log.Log;
import module.status.SeqPedido;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class SEND implements MSG_interface {
  private final Log log;
  private final String dir;
  private int port;
  private final InetAddress clientIP;
  private DatagramSocket socket;
  private DatagramPacket packet;

  private Byte seq = (byte) 0;
  private Byte seqPedido;
  private final SeqPedido controlSeqPedido;

  private final Type type = Type.Send;

  private String fileName;
  private Queue<byte[]> fileInBytes = null;

  private long lastModification = 0;
  private int lastSizeRead = 0;
  private int sizeFile = 0;

  public SEND(InetAddress clientIp, int port, DatagramSocket socket, SeqPedido seqPedido, String fileName, String dir,
              Log log) {
    this.log = log;
    this.port = port;
    this.clientIP = clientIp;
    this.socket = socket;
    this.controlSeqPedido = seqPedido;
    this.dir = dir;
    this.fileName = fileName;
  }

  public SEND(DatagramPacket packet, InetAddress clientIP, int port, DatagramSocket socket, SeqPedido seq, String dir,
              Log log) {
    this.log = log;
    this.dir = dir;
    this.port = port;
    this.clientIP = clientIP;
    this.socket = socket;
    this.controlSeqPedido = seq;
    this.packet = packet;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setSocket(DatagramSocket socket) {
    this.socket = socket;
  }

  @Override
  public DatagramPacket getPacket() {
    return packet;
  }

  @Override
  public int getPort() {
    return socket.getPort();
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public boolean validType(DatagramPacket packet) {
    var msg = packet.getData();
    return msg[0] == Type.Send.getBytes();
  }

  @Override
  public void createTailPacket(byte[] buff) {
    if (fileInBytes.isEmpty()) return;

    byte[] info = fileInBytes.remove();

    int i2 = 0;
    int i = Constantes.CONFIG.HEAD_SIZE;

    for (; i2 < info.length && i < Constantes.CONFIG.BUFFER_SIZE; i++, i2++) {
      buff[i] = info[i2];
    }
    for (; i < Constantes.CONFIG.BUFFER_SIZE; i++)
      buff[i] = (byte) 0;
  }

  public DatagramPacket createPacket() {
    byte[] msg = createMsg(seqPedido, seq);
    seq++;
    return new DatagramPacket(msg, msg.length, clientIP, port);
  }

  private DatagramPacket firstPacket() {
    String s = lastModification + ";;" + lastSizeRead;
    var info = s.getBytes(StandardCharsets.UTF_8);

    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    buff[0] = getType().getBytes();
    buff[1] = seqPedido;
    buff[2] = seq;
    seq++;

    int i2 = 0;
    int i = Constantes.CONFIG.HEAD_SIZE;

    for (; i2 < info.length && i < Constantes.CONFIG.BUFFER_SIZE; i++, i2++) {
      buff[i] = info[i2];
    }
    for (; i < Constantes.CONFIG.BUFFER_SIZE; i++)
      buff[i] = (byte) 0;

    return new DatagramPacket(buff, buff.length, clientIP, port);
  }

  public Queue<DatagramPacket> createPackets() {
    if (readFile() != 0) return new LinkedList<>();

    Queue<DatagramPacket> list = new LinkedList<>();
    list.add(firstPacket());

    int len = fileInBytes.size();
    for (var i = 0; i < len; i++)
      list.add(createPacket());
    return list;
  }

  public void sendFirst(DatagramSocket socket) {
    //TODO
  }

  @Override
  public void send() throws IOException, PackageErrorException {
    this.seqPedido = controlSeqPedido.getSeq();
    Queue<DatagramPacket> packets = createPackets();
    if (packets.isEmpty()) {
      System.out.println("Nao mandei nenhum ficheiro");
      return;
    }
    var startTime = System.currentTimeMillis();
    for (var elem = packets.remove(); elem != null; ) {
      socket.send(elem);
      log.addQueueSend(MSG_interface.MSGToString(elem));
      ACK ack = new ACK(elem, port, socket, clientIP, seq, log);
      boolean ackFail = false;
      int ackError = 0;
      int packageError = 0;
      int timeOutError = 0;
      while (!ackFail) {
        try {
          ack.received();
          ackFail = true;
        } catch (TimeOutMsgException e) {
          timeOutError++;
          if (timeOutError > 5)
            break;
          socket.send(elem);
          log.addQueueSend(MSG_interface.MSGToString(elem));
        } catch (PackageErrorException e1) {
          if (packageError > 3) break;
          packageError++;
          socket.send(elem);
          log.addQueueSend(MSG_interface.MSGToString(elem));
        } catch (AckErrorException e2) {
          if (ackError > 3) break;
          ackError++;
          socket.send(elem);
          log.addQueueSend(MSG_interface.MSGToString(elem));
        }
      }
      if (ackError > 3 || packageError > 3 || timeOutError > 5) break;

      if (!packets.isEmpty())
        elem = packets.remove();
      else
        elem = null;
    }

    ACK ack = new ACK(null, port, socket, clientIP, seqPedido, log);
    seqPedido++;
    try {
      ack.send();
    } catch (Exception e) {
    }

    var endTime = System.currentTimeMillis();
    var time = (endTime - startTime) ;
    // var time = (endTime - startTime) / 1000;
    var bitsSec = sizeFile * 8L / 1000;
    if (time > 0) bitsSec = bitsSec / time;
    var s = "[SEND] Name: " + fileName + " || Size: " + sizeFile + " || Time (ms): " + time +
        " || Bits/sec: " + bitsSec;
    log.addQueueTime(s);
    log.status.increaseSendFiles();
  }

  private void createDirectors(String dir, String fileName) {
    var strings = fileName.split("/");
    if (strings.length == 1)
      return;

    String pasta = strings[0];
    int i = 0;
    for( ; i< strings.length -1 ; i++) {
      if (i!= 0)
        pasta = pasta + "/" + strings[i];
      File file1 = new File( dir + "/" + pasta);
      if (file1.exists())
        continue;
      file1.mkdirs();
    }
  }

  public void writeFile(Queue<byte[]> array, long lastModification) throws IOException {

    FileOutputStream out;
    createDirectors(dir, fileName);
    try {
      out = new FileOutputStream(dir + '/' + fileName);
    } catch (FileNotFoundException e){
      e.printStackTrace();
      return;
    }

    int size = array.size();
    int i = 1;
    for (var data : array) {
      if (i == size) {
        out.write(Arrays.copyOfRange(data, 0, lastSizeRead));
      } else
        out.write(Arrays.copyOfRange(data, 0, data.length - 3));

      i++;
    }
    out.flush();
    out.close();
    File file = new File(dir + '/' + fileName);
    file.setLastModified(lastModification);
  }

  public void send(DatagramSocket socket) throws IOException, PackageErrorException {
    Queue<DatagramPacket> packets = createPackets();
    if (packets.isEmpty()) {
      System.out.println("Nao mandei nenhum ficheiro");
      return;
    }

    for (var elem = packets.remove(); elem != null; ) {
      socket.send(elem);
      log.addQueueSend(MSG_interface.MSGToString(elem));
      ACK ack = new ACK(elem, port, socket, clientIP, seq, log);
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
          socket.send(elem);
          log.addQueueSend(MSG_interface.MSGToString(elem));
        } catch (PackageErrorException e1) {
          if (packageError > 3) break;
          packageError++;
          socket.send(elem);
          log.addQueueSend(MSG_interface.MSGToString(elem));
        } catch (AckErrorException e2) {
          if (ackError > 3) break;
          ackError++;
          socket.send(elem);
          log.addQueueSend(MSG_interface.MSGToString(elem));
        }
      }
      if (ackError > 3 || packageError > 3 || timeOutError > 5) break;

      if (!packets.isEmpty())
        elem = packets.remove();
      else
        elem = null;
    }

    ACK ack = new ACK(null, port, socket, clientIP, seqPedido, log);
    seqPedido++;
    try {
      ack.send();
    } catch (Exception e) {
    }
    try {
      socket.receive(null);
      ack.send();
    } catch (SocketTimeoutException ignored) {
    }
    ;
    log.status.increaseSendFiles();
  }

  @Override
  public void received() throws IOException, TimeOutMsgException, PackageErrorException, AckErrorException {
    boolean fileReceved = false; // so passa a true no ultimo caso
    boolean segFileReceved = false; // so passa a true no ultimo caso

    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
    Queue<byte[]> file = new LinkedList<>();
    byte[] last = null;
    boolean first = true;
    long lastModification = 0;
    while (!fileReceved) {
      segFileReceved = false;
      while (!segFileReceved) {
        ACK ack = null;
        try {
          socket.receive(receivedPacket);

          segFileReceved = validType(receivedPacket);
          if (segFileReceved) {
            log.addQueueReceived(MSG_interface.MSGToString(receivedPacket));
            ack = new ACK(receivedPacket, port, socket, clientIP, controlSeqPedido.getSeq(), log);
            ack.send();
            byte[] data = MSG_interface.getDataMsg(receivedPacket);
            if (first) {
              int i;
              for (i = 0; i < data.length && data[i] != (byte) 0; i++) ;

              String msg = new String(Arrays.copyOfRange(data, 0, i));
              var aux = msg.split(";;", 2);
              lastModification = Long.parseLong(aux[0]);
              this.lastSizeRead = Integer.parseInt(aux[1]);

              first = false;
              continue;
            }
            if (last == null || !Arrays.equals(last, data)) {
              file.add(data.clone());
              last = data.clone();
            }

          } else {
            fileReceved = true;
            break;
          }
        } catch (SocketTimeoutException e) {
          ack = new ACK(receivedPacket, port, socket, clientIP, controlSeqPedido.getSeq(), log);
          ack.send();
        }
      }
    }
    if (!file.isEmpty()) {
      writeFile(file, lastModification);
      log.status.increaseGetFiles();
    }
  }

  // return 0 se sucesso
  public int readFile() {
    this.fileInBytes = new LinkedList<>();
    FileInputStream in;
    File file;
    try {
      in = new FileInputStream(dir + '/' + fileName);
      file = new File(dir + '/' + fileName);
    } catch (FileNotFoundException e) {
      System.out.println("File not found Exception->  " + dir + '/' + fileName );
      System.out.println(e.getMessage());
      return -1;
    }

    int size = Constantes.CONFIG.TAIL_SIZE;
    byte[] buff = new byte[size];
    this.lastModification = file.lastModified();

    int sizeRead;
    int lastSize = 0;
    try {
      while (-1 != (sizeRead = in.read(buff))) {
        lastSize = sizeRead;
        this.sizeFile += sizeRead;
        for (; sizeRead < size; sizeRead++)
          buff[sizeRead] = (byte) 0;
        fileInBytes.add(buff.clone());
        buff = new byte[size];
      }
      this.lastSizeRead = lastSize;
    } catch (IOException e) {
      e.printStackTrace();
      return -1;
    }
    return 0;
  }

  public static String toString(DatagramPacket packet) {
    byte[] msg = packet.getData();
    return "[SEND] -> SEQ: " + msg[1] + "; SEG: " + msg[2] + "; MSG (dados do ficheiro)";
  }
}
