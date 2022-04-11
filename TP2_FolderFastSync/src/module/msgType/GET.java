package module.msgType;

import interfaces.MSG_interface;
import module.Constantes;
import module.directory.FileStruct;
import module.exceptions.AckErrorException;
import module.exceptions.PackageErrorException;
import module.exceptions.TimeOutMsgException;
import module.log.Log;
import module.status.SeqPedido;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class GET implements MSG_interface {

  private final Log log;
  private final String dir;
  private int port;
  private final InetAddress clientIP;
  private DatagramSocket socket;

  private Byte seq = (byte) 0;
  private final SeqPedido seqPedido;

  private final Type type = Type.Get;

  private FileStruct file;
  private DatagramPacket packet;


  public GET(InetAddress clientIp, int port, DatagramSocket socket, SeqPedido seqPedido, FileStruct file,
             String dir, Log log) {
    this.log = log;
    this.port = port;
    this.clientIP = clientIp;
    this.socket = socket;
    this.seqPedido = seqPedido;
    this.file = file;
    this.dir = dir;
    this.packet = null;
  }

  public GET(DatagramPacket packet, InetAddress clientIP, int port, DatagramSocket socket, SeqPedido seq, String dir,
             Log log) {
    this.log = log;
    this.dir = dir;
    this.port = port;
    this.clientIP = clientIP;
    this.socket = socket;
    this.seqPedido = seq;
    this.file = null;
    this.packet = packet;
  }

  public DatagramPacket getPacket() {
    return packet;
  }

  @Override
  public void setPort(int port) {
    this.port = port;
  }

  @Override
  public void setSocket(DatagramSocket socket) {
    this.socket = socket;
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
    return msg[0] == Type.Get.getNum();
  }

  @Override
  public void createTailPacket(byte[] buff) {
    if (file == null)
      return;
    String name = file.getName();
    int i2 = 0;
    int i = Constantes.CONFIG.HEAD_SIZE;
    byte[] string = name.getBytes(StandardCharsets.UTF_8);
    for (; i2 < string.length && i < Constantes.CONFIG.BUFFER_SIZE; i++, i2++) {
      buff[i] = string[i2];
    }

    for (; i < Constantes.CONFIG.BUFFER_SIZE; i++)
      buff[i] = 0;

    buff[Constantes.CONFIG.BUFFER_SIZE - 1] = 0;
  }

  public DatagramPacket createPacket() {
    byte[] msg = createMsg(seqPedido.getSeq(), seq);
    seq++;
    return new DatagramPacket(msg, msg.length, clientIP, port);
  }

  public void atualizaFileName(DatagramPacket packet) {
    File file_ = new File(getFilename(packet));
    this.file = new FileStruct(file_);
  }

  public String getFilename(DatagramPacket packet) {
    byte[] msg = MSG_interface.getDataMsg(packet);
    int i;
    for (i = 0; i < msg.length && msg[i] != 0; i++) ;

    return new String(msg, 0, i, StandardCharsets.UTF_8);
  }

  @Override
  public void send() throws IOException, PackageErrorException {
    DatagramPacket packet = createPacket();
    socket.send(packet);
    log.addQueueSend(MSG_interface.MSGToString(packet));

    ACK ack = new ACK(packet, port, socket, clientIP, seq, log);
    boolean ackFail = false;
    while (!ackFail) {
      try {
        ack.received();
        ackFail = true;
      } catch (TimeOutMsgException | AckErrorException e) {
        socket.send(packet);
        log.addQueueSend(MSG_interface.MSGToString(packet));
      } catch (PackageErrorException e1) {
        break;
      }
    }

    port = ack.getPort();

    if (file == null)
      return; // TODO lancar erro ou mandar o bye

    SEND SENDMsg = new SEND(clientIP, port, socket, seqPedido, file.getName(), dir, log);
    try {
      SENDMsg.received();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public void sendFirst(DatagramSocket socket) throws IOException {
    DatagramPacket packet = createPacket();
    socket.send(packet);
    log.addQueueSend(MSG_interface.MSGToString(packet));
    this.packet = packet;
  }

  @Override
  public void send(DatagramSocket socket) throws IOException, PackageErrorException {
    ACK ack = new ACK(this.packet, port, socket, clientIP, seq, log);
    boolean ackFail = false;
    while (!ackFail) {
      try {
        ack.received();
        ackFail = true;
      } catch (TimeOutMsgException | AckErrorException e) {
        packet.setPort(port);
        socket.send(packet);
        log.addQueueSend(MSG_interface.MSGToString(packet));
      } catch (PackageErrorException e1) {
        break;
      }
    }

    port = ack.getPort();

    if (file == null)
      return;

    SEND SENDMsg = new SEND(clientIP, port, socket, seqPedido, file.getName(), dir, log);
    try {
      SENDMsg.received();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void received() throws IOException, TimeOutMsgException, PackageErrorException, AckErrorException {
    String filename = getFilename(packet);

    if (filename == null) {
      System.out.println("O filename recebido é nulo");
      return; //TODO send bye
    }
    SEND SENDMsg = new SEND(clientIP, port, socket, seqPedido, filename, dir, log);
    try {
      SENDMsg.send();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public static String toString(DatagramPacket packet) {
    byte[] msg = packet.getData();
    byte[] dados = MSG_interface.getDataMsg(packet);
    int i;
    for (i = 0; i < dados.length && dados[i] != (byte) 0; i++) ;
    return "[GET]  -> SEQ: " + msg[1] + "; SEG: " + msg[2] + "; MSG: "
        + new String(Arrays.copyOfRange(dados, 0, i));
  }

  @Override
  public String toString() {
    return toString(this.packet);
  }
}
