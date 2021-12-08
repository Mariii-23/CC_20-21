package module.MsgType;

import module.Constantes;
import module.Exceptions.AckErrorException;
import module.Exceptions.PackageErrorException;
import module.Exceptions.TimeOutMsgException;
import module.MSG_interface;
import module.Status.FileStruct;
import module.Type;

import javax.swing.text.html.Option;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class SEND implements MSG_interface {

  String dir;
  private int port;
  InetAddress clientIP;
  DatagramSocket socket;

  Byte seq;
  Byte seqPedido;

  Type type = Type.Send;

  Optional<FileStruct> file;


  public SEND(InetAddress clientIp, int port, DatagramSocket socket, Byte seqPedido, FileStruct file,String dir) {
    this.seq = (byte) 0;
    this.port = port;
    this.clientIP = clientIp;
    this.socket = socket;
    this.seqPedido = seqPedido;
    this.file = Optional.ofNullable(file);
    //this.fileName = file.getName();
    this.dir = dir;
  }

  public SEND(InetAddress clientIP,int port,DatagramSocket socket, byte seq, String dir){
    this.seq = (byte) 0;
    this.dir = dir;
    this.port = port;
    this.clientIP = clientIP;
    this.socket = socket;
    this.seqPedido = seq;
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
    return msg[0] == Type.Send.getNum();
  }

  @Override
  public void createTailPacket(byte[] buff) {
    String name = file.get().getName();
    int i2=0;
    int i = Constantes.CONFIG.HEAD_SIZE - 1;
    //System.out.println(Constantes.CONFIG.BUFFER_SIZE + "  BUFFER ->   "+ info.length);
    byte[] string = name.getBytes(StandardCharsets.UTF_8);
    for (; i2 < string.length && i < Constantes.CONFIG.BUFFER_SIZE ; i++,i2++ ){
      buff[i] = string[i2];
    }
  }

  public DatagramPacket createPacket() {
    byte[] msg = createMsg(seqPedido,seq); seq++;
    return new DatagramPacket(msg, msg.length, clientIP ,port);
  }

  public void atualizaFileName(DatagramPacket packet){
    byte[] msg = MSG_interface.getDataMsg(packet);
    String filename = new String(msg, StandardCharsets.UTF_8);
    File file_ = new File(filename);
    this.file = Optional.of(new FileStruct(file_));
  }

  @Override
  public void send() throws IOException, PackageErrorException {
    System.out.println("vou mandar o pedido file");
    DatagramPacket packet = createPacket();
    if(validType(packet))
      System.out.println("pacote direito");
    socket.send(packet);

    ACK ack = new ACK(packet,port,socket,clientIP,seqPedido); seqPedido++;
    boolean ackFail = false;
    while (!ackFail) {
      try {
        ack.received();
        ackFail = true;
      } catch (TimeOutMsgException e) {
        socket.send(packet);
        //continue;
      } catch (PackageErrorException e1) {
        break;
        //socket.send(packet);
        //continue;
      } catch (AckErrorException e2) {
        socket.send(packet);
      }
    }

    System.out.println("Estou a receber o file");
    GET getMsg = new GET(clientIP,port,socket,++seq,file.get().getName(),dir);
    try {
      getMsg.received();
    }catch (Exception e){
      System.out.println(e);
    }
  }

  @Override
  public void received() throws IOException, TimeOutMsgException, PackageErrorException, AckErrorException {

    System.out.println("vou receber um pedido para mandar o ficheiro");
    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
    socket.receive(receivedPacket);
    System.out.println("dentro: ");
    MSG_interface.printMSG(receivedPacket);
    if (validType(receivedPacket)){
      //ACK ack = new ACK(receivedPacket,port,socket,clientIP,seqPedido); seqPedido++;
      //ack.send();
      ACK ack = new ACK(receivedPacket,port,socket,clientIP,seqPedido); seqPedido++;
      ack.send();
      atualizaFileName(receivedPacket);

      System.out.println("Vou mandar o file");
      GET getMsg = new GET(clientIP,port,socket,++seq,file.get(),dir);
      //try {
        getMsg.send();
        System.out.println("send Get ");
      //}catch (Exception e){
      //  System.out.println(e);
      //}
    } else {
      System.out.println("FODA SEEEE");
    }
  }

  public static String toString(DatagramPacket packet){
    byte[] msg = packet.getData();
    return  "SEND: " + msg[1] + " SEG: " +msg[2] + "filename: "+ new String(MSG_interface.getDataMsg(packet));
  }
}
