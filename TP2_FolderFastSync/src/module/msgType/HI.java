package module.msgType;

import interfaces.MSG_interface;
import module.Constantes;
import module.exceptions.AckErrorException;
import module.exceptions.AutenticationFailed;
import module.exceptions.PackageErrorException;
import module.exceptions.TimeOutMsgException;
import module.log.Log;
import module.status.SeqPedido;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class HI implements MSG_interface {

  private int port;
  private final Log log;

  private final InetAddress clientIP;

  private final Type type = Type.Hi;
  private DatagramPacket packet;
  private DatagramSocket socket;

  private Byte seq = (byte) 0;
  private final SeqPedido seqPedido;

  boolean known_port;

  private String msgToSend;

  public HI(InetAddress clientIp, int port, DatagramSocket socket, SeqPedido seq, Log log) throws SocketException {
    this.log = log;
    this.clientIP = clientIp;
    this.port = port;
    this.socket = socket;
    this.packet = null;
    this.seqPedido = seq;
    known_port = true;
  }

  public HI(DatagramPacket packet, int port, DatagramSocket socket, SeqPedido seq, Log log) throws SocketException {
    this.log = log;
    this.port = port;
    this.clientIP = packet.getAddress();
    this.packet = packet;
    this.socket = socket;
    this.seqPedido = seq;
    known_port = false;
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
    return port;
  }

  @Override
  public Type getType() {
    return type;
  }

  public void createTailPacket(byte[] buff) {
    byte[] msgByte;
    if (msgToSend == null)
      msgByte = "ERROR_NOTHING_SEND;;".getBytes(StandardCharsets.UTF_8);
    else
      msgByte = (msgToSend + ";;").getBytes(StandardCharsets.UTF_8);

    int i2 = 0;
    int i;
    for (i = Constantes.CONFIG.HEAD_SIZE; i2 < msgByte.length; i++, i2++)
      buff[i] = msgByte[i2];
    for (; i < buff.length; i++)
      buff[i] = (byte) 0;
  }

  //@Override
  public DatagramPacket createPacket(byte seq, byte seqSeg) {
    byte[] msg = createMsg(seq, seqSeg);
    return this.packet = new DatagramPacket(msg, msg.length, clientIP, port);
  }

  public boolean validType(DatagramPacket packet) {
    byte[] msg = packet.getData();
    return msg[0] == Type.Hi.getBytes();
  }

  public static boolean valid(DatagramPacket packet) {
    byte[] msg = packet.getData();
    return msg[0] == Type.Hi.getBytes();
  }

  public boolean validateAuthentication(DatagramPacket packet) {

    var msg = new String(MSG_interface.getDataMsg(packet), StandardCharsets.UTF_8);
    var receivedValue = msg.split(";;", 0)[0];
    receivedValue = receivedValue.replace(";;", "");

    return log.status.validate(msgToSend, receivedValue);
  }

  public void sendFirst(DatagramSocket socket) throws IOException {
    atualizaMsgToSend(null);
    var sendPackage = createPacket(seqPedido.getSeq(), seq);
    socket.send(sendPackage);
    log.addQueueSend(MSG_interface.MSGToString(packet));
    seq++;
  }

  public void atualizaMsgToSend(String key) {
    if (key == null) {
      this.msgToSend = log.status.generateKey();
    } else {
      this.msgToSend = log.status.getValue(key);
      if (msgToSend == null)
        msgToSend = "ERROR";
    }
  }

  public void send() throws IOException, PackageErrorException, AutenticationFailed {
    atualizaMsgToSend(null);

    var sendPackage = createPacket(seqPedido.getSeq(), seq);
    socket.send(sendPackage);
    log.addQueueSend(MSG_interface.MSGToString(packet));
    seq++;

    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
    socket.setSoTimeout(2000);

    boolean receivedPackage = false;
    int i = 0;
    while (!receivedPackage) {
      try {
        socket.receive(receivedPacket);
        if (!known_port)
          port = receivedPacket.getPort();

      } catch (IOException e) {
        throw new SocketTimeoutException("Não recebeu nada");
      }

      if (!validType(receivedPacket)) {
        if (i > 3) return;
        i++;
      } else {
        if (!validateAuthentication(receivedPacket)) {
          log.addQueueReceived(MSG_interface.MSGToString(receivedPacket));
          BYE bye = new BYE(packet, clientIP, port, socket, seqPedido, log.status, log);
          try {
            bye.send();
          } catch (TimeOutMsgException ignored) {
          }
          throw new AutenticationFailed();
        }
        log.addQueueReceived(MSG_interface.MSGToString(receivedPacket));
        ACK ack = new ACK(receivedPacket, port, socket, clientIP, seqPedido.getSeq(), log);
        ack.send();
        receivedPackage = true;
      }
    }
  }

  public void send(DatagramSocket socket) throws IOException, SocketTimeoutException, PackageErrorException {

    var sendPackage = createPacket(seqPedido.getSeq(), seq);
    this.socket.send(sendPackage);
    seq++;

    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
    this.socket.setSoTimeout(2000);

    boolean receivedPackage = false;
    int i = 0;
    while (!receivedPackage) {
      try {
        this.socket.receive(receivedPacket);
        if (!known_port)
          port = receivedPacket.getPort();

      } catch (IOException e) {
        throw new SocketTimeoutException("Não recebeu nada");
      }

      if (!validType(receivedPacket)) {
        if (i > 3) return;
        i++;
      } else {
        log.addQueueReceived(MSG_interface.MSGToString(receivedPacket));
        ACK ack = new ACK(receivedPacket, port, socket, clientIP, seqPedido.getSeq(), log);
        ack.send();
        receivedPackage = true;
      }
    }
  }

  public void received() throws IOException, AutenticationFailed {

    boolean hiReceived = false;

    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
    int i = 0;
    while (!hiReceived) {
      try {
        socket.receive(receivedPacket);
        if (!known_port)
          port = receivedPacket.getPort();

        hiReceived = validType(receivedPacket);
        if (hiReceived) {
          log.addQueueReceived(MSG_interface.MSGToString(receivedPacket));
          var msg = new String(MSG_interface.getDataMsg(receivedPacket), StandardCharsets.UTF_8);
          var reciviedeKey = msg.split(";;", 0)[0];
          reciviedeKey = reciviedeKey.replace(";;", "");
          atualizaMsgToSend(reciviedeKey);
        } else {
          if (i > 3) return;
          i++;
        }
      } catch (SocketTimeoutException e) {
        continue;
      }
    }

    boolean hiMsgReceived = false;
    while (!hiMsgReceived) {
      DatagramPacket hiPacket = createPacket(seqPedido.getSeq(), seq);
      seq++;
      socket.send(hiPacket);
      log.addQueueSend(MSG_interface.MSGToString(packet));

      ACK ack = new ACK(hiPacket, port, socket, clientIP, seq, log);
      boolean ackFail = false;
      while (!ackFail) {
        try {
          ack.received();
          ackFail = true;
          hiMsgReceived = true;
        } catch (TimeOutMsgException e) {
          continue;
        } catch (PackageErrorException e1) {
          ACK ack1 = new ACK(ack.getPacket(), port, socket, clientIP, seq, log);
          ack1.send();
          throw new AutenticationFailed();
        } catch (AckErrorException e2) {
          break;
        }
      }
    }
  }

  public String toString() {
    if (packet != null)
      return HI.toString(packet);
    else
      return "Packet Invalid";
  }

  public static String toString(DatagramPacket packet) {
    byte[] msg = packet.getData();
    return "[HI]   -> SEQ: " + msg[1] + "; SEG: " + msg[2] + "; MSG: HI";
  }
}
