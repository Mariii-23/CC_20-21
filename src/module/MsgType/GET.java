package module.MsgType;

import module.Constantes;
import module.Exceptions.AckErrorException;
import module.Exceptions.PackageErrorException;
import module.Exceptions.TimeOutMsgException;
import module.MSG_interface;
import module.Status.FileStruct;
import module.Type;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class GET implements MSG_interface {

  private final String dir;
  private final int port;
  private final InetAddress clientIP;
  private final DatagramSocket socket;

  private Byte seq;
  private Byte seqPedido;

  private final Type type = Type.Get;

  private FileStruct file;
  private final DatagramPacket packet;


  public GET(InetAddress clientIp, int port, DatagramSocket socket, Byte seqPedido, FileStruct file, String dir) {
    this.seq = (byte) 0;
    this.port = port;
    this.clientIP = clientIp;
    this.socket = socket;
    this.seqPedido = seqPedido;
    this.file = file;
    this.dir = dir;
    this.packet = null;
  }

  public GET(InetAddress clientIP, int port, DatagramSocket socket, byte seq, String dir){
    this.seq = (byte) 0;
    this.dir = dir;
    this.port = port;
    this.clientIP = clientIP;
    this.socket = socket;
    this.seqPedido = seq;
    this.file = null;
    this.packet = null;
  }

  public GET(DatagramPacket packet,InetAddress clientIP, int port, DatagramSocket socket, byte seq, String dir){
    this.seq = (byte) 0;
    this.dir = dir;
    this.port = port;
    this.clientIP = clientIP;
    this.socket = socket;
    this.seqPedido = seq;
    this.file = null;
    this.packet = packet;
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
    int i = Constantes.CONFIG.HEAD_SIZE - 1;
    byte[] string = name.getBytes(StandardCharsets.UTF_8);
    for (; i2 < string.length && i < Constantes.CONFIG.BUFFER_SIZE ; i++,i2++ ){
      buff[i] = string[i2];
    }

    for( ;i < Constantes.CONFIG.BUFFER_SIZE; i++)
      buff[i] = 0;

    buff[Constantes.CONFIG.BUFFER_SIZE-1] = 0;
  }

  public DatagramPacket createPacket() {
    byte[] msg = createMsg(seqPedido,seq); seq++;
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

    ACK ack = new ACK(packet,port,socket,clientIP,seqPedido); seqPedido++;
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

    if (file == null)
      return; // TODO lancar erro ou mandar o bye

    SEND SENDMsg = new SEND(clientIP,port,socket,++seq,file.getName(),dir);
    try {
      SENDMsg.received();
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  @Override
  public void received() throws IOException, TimeOutMsgException, PackageErrorException, AckErrorException {
    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    //DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
    boolean receivedRightPacket = false;
    //int errors = -1;
    //while ( !receivedRightPacket && errors < 3 ){
      //errors++;
      //socket.receive(receivedPacket);
      //System.out.print("RECEBI: ");
      //MSG_interface.printMSG(receivedPacket);
      //receivedRightPacket = validType(receivedPacket);
    //}
    //receivedRightPacket = validType(packet);
    System.out.print("RECEBI: ");
    MSG_interface.printMSG(packet);

    ACK ack = new ACK(packet,port,socket,clientIP,seqPedido); seqPedido++;
    ack.send();
    //atualizaFileName(receivedPacket);
    String filename = getFilename(packet);

    if (filename == null){
      System.out.println("O filename recebido é nulo");
      return; //TODO send bye
    }

    SEND SENDMsg = new SEND(clientIP,port,socket,++seq,filename,dir);
    try {
      SENDMsg.send();
    }catch (Exception e){
      System.out.println(e);
    }
  }

  public static String toString(DatagramPacket packet){
    byte[] msg = packet.getData();
    return  "[GET]  -> SEQ:" + msg[1] + "; SEG: " +msg[2] + "; MSG: "
        + new String(MSG_interface.getDataMsg(packet));
  }
}
