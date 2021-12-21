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

  private InetAddress clientIP; // coisas que nao estao a ser bem usadas

  private Type type = Type.Hi;
  private DatagramPacket packet;
  private DatagramSocket socket;

  private Byte seq = (byte) 0;
  private SeqPedido seqPedido;

  boolean know_port;

  //private String key;
  private String msgToSend;

  public HI(InetAddress clientIp, int port, DatagramSocket socket, SeqPedido seq, Log log) throws SocketException {
    this.log = log;
    //this.serverIP = serverIP;
    this.clientIP = clientIp;
    this.port = port;
    this.socket = socket;
    this.packet = null;
    this.seqPedido = seq;
    know_port = true;
    //this.serverSocket = new DatagramSocket();
  }

  public HI(DatagramPacket packet, int port, DatagramSocket socket, SeqPedido seq, Log log) throws SocketException {
    this.log = log;
    this.port = port;
    this.clientIP = packet.getAddress();
    this.packet = packet;
    this.socket = socket;
    this.seqPedido = seq;
    know_port = false;
    //this.serverSocket = new DatagramSocket();
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
    //   var key = log.status.generateKey();
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

  //TODO
  public boolean validadeAutentication(DatagramPacket packet) {

    var msg = new String(MSG_interface.getDataMsg(packet), StandardCharsets.UTF_8);
    //System.out.println(msg);
    var reciviedeValue = msg.split(";;", 0)[0];
    reciviedeValue = reciviedeValue.replace(";;", "");
    //System.out.println("RECEBI o valor :" + reciviedeValue);

    return log.status.validate(msgToSend, reciviedeValue);
  }

  public void sendFirst(DatagramSocket socket) throws IOException {
    atualizaMsgToSend(null);
    var sendPackage = createPacket(seqPedido.getSeq(), seq);
    socket.send(sendPackage);
    log.addQueueSend(MSG_interface.MSGToString(packet));
    //log.addQueue(MSG_interface.);
    seq++;
  }

  public void atualizaMsgToSend(String key) {
    if (key == null) {
      this.msgToSend = log.status.generateKey();
      //System.out.println(msgToSend);
      //this.key = msgToSend;
      //System.out.println("KEY ->" + msgToSend);
    } else {
      //System.out.println("Key received ->" + key);
      this.msgToSend = log.status.getValue(key);
      if (msgToSend == null)
        msgToSend = "ERROR";
      //System.out.println("Value ->" + msgToSend);
    }
  }

  public void send() throws IOException, PackageErrorException, AutenticationFailed {
    atualizaMsgToSend(null);

    //TODO mudar esta parte
    var sendPackage = createPacket(seqPedido.getSeq(), seq);
    socket.send(sendPackage);
    log.addQueueSend(MSG_interface.MSGToString(packet));
    seq++;

    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
    socket.setSoTimeout(2000); // prob eliminar e por isso no Communication

    boolean receveidPackage = false;
    while (!receveidPackage) {
      try {
        socket.receive(receivedPacket);
        if (!know_port)
          port = receivedPacket.getPort();

      } catch (IOException e) {
        throw new SocketTimeoutException("Não recebeu nada");
      }

      if (!validType(receivedPacket)) {
        //TODO chamar controlo de fluxo
        // mais q 3 vezes e ele manda um package error
      } else {
        if (!validadeAutentication(receivedPacket)) {
          //System.out.println("erro ao autenticar");
          log.addQueueReceived(MSG_interface.MSGToString(receivedPacket));
          BYE bye = new BYE(packet, clientIP, port, socket, seqPedido, log.status, log);
          try {
            bye.send();
          } catch (TimeOutMsgException ignored) {
          }
          throw new AutenticationFailed();
        }
        //System.out.println("RECEBI: " + HI.toString(receivedPacket));
        log.addQueueReceived(MSG_interface.MSGToString(receivedPacket));
        ACK ack = new ACK(receivedPacket, port, socket, clientIP, seqPedido.getSeq(), log);
        ack.send();
        receveidPackage = true;
      }
    }
  }

  public void send(DatagramSocket socket) throws IOException, SocketTimeoutException, PackageErrorException {

    var sendPackage = createPacket(seqPedido.getSeq(), seq);
    this.socket.send(sendPackage);
    seq++;

    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
    //TODO
    this.socket.setSoTimeout(2000);

    boolean receveidPackage = false;
    while (!receveidPackage) {
      try {
        this.socket.receive(receivedPacket);
        //validateAutentication(receivedPacket);
        if (!know_port)
          port = receivedPacket.getPort();

      } catch (IOException e) {
        throw new SocketTimeoutException("Não recebeu nada");
      }

      if (!validType(receivedPacket)) {
        //TODO chamar controlo de fluxo
        // mais q 3 vezes e ele manda um package error
      } else {
        //System.out.println("RECEBI: " + HI.toString(receivedPacket));
        log.addQueueReceived(MSG_interface.MSGToString(receivedPacket));
        ACK ack = new ACK(receivedPacket, port, socket, clientIP, seqPedido.getSeq(), log);
        ack.send();
        receveidPackage = true;
      }
    }
  }

  public void received() throws IOException, AutenticationFailed {

    boolean hiReceved = false;

    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
    while (!hiReceved) {
      try {
        socket.receive(receivedPacket);
        if (!know_port)
          port = receivedPacket.getPort();

        hiReceved = validType(receivedPacket);
        //TODO se for falso varias vezes temos q fazer algo
        // FLUXO de congestao
        if (hiReceved) {
          log.addQueueReceived(MSG_interface.MSGToString(receivedPacket));
          var msg = new String(MSG_interface.getDataMsg(receivedPacket), StandardCharsets.UTF_8);
          //System.out.println(msg);
          var reciviedeKey = msg.split(";;", 0)[0];
          reciviedeKey = reciviedeKey.replace(";;", "");
          atualizaMsgToSend(reciviedeKey);
        }

      } catch (SocketTimeoutException e) {
        // TODO fluxo de congestao
        continue;
      }
    }

    boolean hiMsgReceveid = false;
    while (!hiMsgReceveid) {
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
          hiMsgReceveid = true;
        } catch (TimeOutMsgException e) {
          // TODO controlo de fluxo
          // vamos diminuindo o tempo de receber cenas
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
    //var msg_ = MSG_interface.getDataMsg(packet);
    //int i =0;
    //for (; i < msg_.length && msg_[i] != (byte) 0; i++);
    //var reciviedeKey = new String(Arrays.copyOfRange(msg,0,i));
    return "[HI]   -> SEQ: " + msg[1] + "; SEG: " + msg[2] + "; MSG: HI"; //+ reciviedeKey;
  }
}
