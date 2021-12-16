package module.MsgType;

import control.SeqPedido;
import module.Constantes;
import module.Exceptions.AckErrorException;
import module.Exceptions.PackageErrorException;
import module.Exceptions.TimeOutMsgException;
import module.MSG_interface;
import module.Status.FileStruct;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class GET implements MSG_interface {

  private final String dir;
  private int port;
  private final InetAddress clientIP;
  private DatagramSocket socket;

  private Byte seq = (byte) 0;
  private final SeqPedido seqPedido;

  private final Type type = Type.Get;

  private FileStruct file;
  private DatagramPacket packet;


  public GET(InetAddress clientIp, int port, DatagramSocket socket, SeqPedido seqPedido, FileStruct file, String dir) {
    this.port = port;
    this.clientIP = clientIp;
    this.socket = socket;
    this.seqPedido = seqPedido;
    this.file = file;
    this.dir = dir;
    this.packet = null;
  }

  public GET(DatagramPacket packet,InetAddress clientIP, int port, DatagramSocket socket, SeqPedido seq, String dir){
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
    if(file == null)
      return;
    String name = file.getName();
    int i2=0;
    int i = Constantes.CONFIG.HEAD_SIZE;
    byte[] string = name.getBytes(StandardCharsets.UTF_8);
    for (; i2 < string.length && i < Constantes.CONFIG.BUFFER_SIZE ; i++,i2++ ){
      buff[i] = string[i2];
    }

    for( ;i < Constantes.CONFIG.BUFFER_SIZE; i++)
      buff[i] = 0;

    buff[Constantes.CONFIG.BUFFER_SIZE-1] = 0;
  }

  public DatagramPacket createPacket() {
    byte[] msg = createMsg(seqPedido.getSeq(),seq); seq++;
    return new DatagramPacket(msg, msg.length, clientIP ,port);
  }

  public void atualizaFileName(DatagramPacket packet){
    File file_ = new File(getFilename(packet));
    this.file = new FileStruct(file_);
  }

  public String getFilename(DatagramPacket packet){
    byte[] msg = MSG_interface.getDataMsg(packet);
    int i;
    for (i = 0; i < msg.length && msg[i] != 0; i++);

    return new String(msg, 0, i,StandardCharsets.UTF_8);
  }

  @Override
  public void send() throws IOException, PackageErrorException {
    DatagramPacket packet = createPacket();
    socket.send(packet);

    ACK ack = new ACK(packet,port,socket,clientIP,seq);
    boolean ackFail = false;
    while (!ackFail) {
      try {
        ack.received();
        ackFail = true;
      } catch (TimeOutMsgException | AckErrorException e) {
        socket.send(packet);
        //continue;
      } catch (PackageErrorException e1) {
        break;
        //socket.send(packet);
        //continue;
      }
    }

    port = ack.getPort();

    if (file == null)
      return; // TODO lancar erro ou mandar o bye

    SEND SENDMsg = new SEND(clientIP,port,socket,seqPedido,file.getName(),dir);
    try {
      SENDMsg.received();
    }catch (Exception e){
      e.printStackTrace();
    }
  }


  public void sendFirst(DatagramSocket socket) throws IOException {
    DatagramPacket packet = createPacket();
    socket.send(packet);
    this.packet = packet;
  }

  @Override
  public void send(DatagramSocket socket) throws IOException, PackageErrorException {
    //DatagramPacket packet = createPacket();
    //socket.send(packet);
    // TODO o packet é nulo porque tenho q lho dar
    ACK ack = new ACK(this.packet,port,socket,clientIP,seq);
    boolean ackFail = false;
    while (!ackFail) {
      try {
        ack.received();
        ackFail = true;
      } catch (TimeOutMsgException | AckErrorException e) {
        socket.send(packet);
        //continue;
      } catch (PackageErrorException e1) {
        break;
        //socket.send(packet);
        //continue;
      }
    }

    port = ack.getPort();

    if (file == null)
      return; // TODO lancar erro ou mandar o bye

    SEND SENDMsg = new SEND(clientIP,port,socket,seqPedido,file.getName(),dir);
    try {
      SENDMsg.received();
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  @Override
  public void received() throws IOException, TimeOutMsgException, PackageErrorException, AckErrorException {
    //byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    //DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
    //boolean receivedRightPacket = false;
    //int errors = -1;
    //while ( !receivedRightPacket && errors < 3 ){
      //errors++;
      //socket.receive(receivedPacket);
      //System.out.print("RECEBI: ");
      //MSG_interface.printMSG(receivedPacket);
      //receivedRightPacket = validType(receivedPacket);
    //}
    //receivedRightPacket = validType(packet);
    //System.out.print("RECEBI: ");
    //MSG_interface.printMSG(packet);

    //ACK ack = new ACK(packet,port,socket,clientIP,SeqPedido); SeqPedido++;
    //ack.send();
    //atualizaFileName(receivedPacket);
    String filename = getFilename(packet);

    if (filename == null){
      System.out.println("O filename recebido é nulo");
      return; //TODO send bye
    }
    SEND SENDMsg = new SEND(clientIP,port,socket,seqPedido,filename,dir);
    try {
      SENDMsg.send();
    }catch (Exception e){
      System.out.println(e);
    }
  }

  public static String toString(DatagramPacket packet){
    byte[] msg = packet.getData();
    return  "[GET]  -> SEQ: " + msg[1] + "; SEG: " +msg[2] + "; MSG: "
        + new String(MSG_interface.getDataMsg(packet));
  }

  @Override
  public String toString() {
    return toString(this.packet);
  }
}
