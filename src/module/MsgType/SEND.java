package module.MsgType;

import module.Constantes;
import module.Exceptions.AckErrorException;
import module.Exceptions.PackageErrorException;
import module.Exceptions.TimeOutMsgException;
import module.MSG_interface;
import module.Type;

import java.io.*;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;

//TODO mudar o nome este é o send file
public class SEND implements MSG_interface {

    String dir;
    private final int port;
    InetAddress clientIP;
    DatagramSocket socket;
    DatagramPacket packet;

    Byte seq;
    Byte seqPedido;

    Type type = Type.Send;

    String fileName;
    Queue<byte[]> fileInBytes = null;

  public SEND(InetAddress clientIp, int port, DatagramSocket socket, Byte seqPedido, String fileName, String dir) {
    this.seq = (byte) 0;
    this.port = port;
    this.clientIP = clientIp;
    this.socket = socket;
    this.seqPedido = seqPedido;
    this.dir = dir;
    this.fileName = fileName;
  }

  public SEND(InetAddress clientIP, int port, DatagramSocket socket, byte seq, String fileName, String dir){
    this.seq = (byte) 0;
    this.dir = dir;
    this.port = port;
    this.clientIP = clientIP;
    this.socket = socket;
    this.seqPedido = seq;
    this.fileName = fileName;
  }

  public SEND(DatagramPacket packet,InetAddress clientIP, int port, DatagramSocket socket, byte seq, String dir){
    this.seq = (byte) 0;
    this.dir = dir;
    this.port = port;
    this.clientIP = clientIP;
    this.socket = socket;
    this.seqPedido = seq;
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
    return msg[0] == Type.Send.getBytes();
  }

  @Override
  public void createTailPacket(byte[] buff) {
    if (fileInBytes.isEmpty()) return;

    byte[] info = fileInBytes.remove();
    int i2=0;
    int i = Constantes.CONFIG.HEAD_SIZE - 1;

    for (; i2 < info.length && i < Constantes.CONFIG.BUFFER_SIZE ; i++,i2++ ){
      buff[i] = info[i2];
    }
  }

  public DatagramPacket createPacket() {
    byte[] msg = createMsg(seqPedido,seq); seq++;
    return new DatagramPacket(msg, msg.length, clientIP ,port);
  }


  public Queue<DatagramPacket> createPackets() {
    // isto nao devia acontecer
    if (readFile() != 0) return new LinkedList<>();

    Queue<DatagramPacket> list = new LinkedList<>();
    int len = fileInBytes.size();
    for (var i =0 ; i < len  ;i++)
      list.add(createPacket());
    return list;
  }

  @Override
  public void send() throws IOException, PackageErrorException {
    Queue<DatagramPacket> packets = createPackets();
    if (packets.isEmpty()) {
      System.out.println("Nao mandei nenhum ficheiro");
      //TODO Acrescentar cenas ou mudar
      // mandar um bye
      return;
    }

    for(var elem = packets.remove(); elem !=null;){
      socket.send(elem);
      ACK ack = new ACK(elem,port,socket,clientIP,seqPedido); seqPedido++;
      boolean ackFail = false;
      int ackError=0;
      int packageError=0;
      int timeOutError=0;
      while (!ackFail) {
        try {
          ack.received();
          ackFail = true;
        } catch (TimeOutMsgException e) {
          if (timeOutError>5)
            break;
          timeOutError++;
          // TODO controlo de fluxo
          // vamos diminuindo o tempo de receber cenas
          socket.send(elem);
          //continue;
        } catch (PackageErrorException e1) {
          // TODO controlo de fluxo
          // a partir de x pacotes errados, fechamos a conecao
          if (packageError > 3) break;
          packageError++;
          socket.send(elem);
          //continue;
        } catch (AckErrorException e2) {
          if (ackError>3) break;
          ackError++;
          socket.send(elem);
        }
      }
      if (ackError>3 || packageError>3 || timeOutError>5) break;

      if(!packets.isEmpty())
        elem= packets.remove();
      else
        elem = null;
    }

    //TODO MUDAR
    ACK ack = new ACK(null,port,socket,clientIP,seqPedido); seqPedido++;
    try {
      ack.send();
    } catch (Exception e){
    }
  }

    public void writeFile(Queue<byte[]> array) throws IOException {
      FileWriter myWriter = new FileWriter(dir+'/'+fileName);

      for(var data : array){
        String s = new String(data, StandardCharsets.UTF_8);
        myWriter.write(s);
      }
      myWriter.flush();
      myWriter.close();
  }

  @Override
  public void received() throws IOException, TimeOutMsgException, PackageErrorException, AckErrorException {
    // recebe o pedido
    boolean fileReceved = false; // so passa a true no ultimo caso
    boolean segFileReceved = false; // so passa a true no ultimo caso

    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
    Queue<byte[]> file = new LinkedList<>();
    int i =0;
    while (!fileReceved) {
      segFileReceved = false;
      while (!segFileReceved){
        try {
          socket.receive(receivedPacket);
          i++;
          segFileReceved = validType(receivedPacket);
          if (segFileReceved) {
            System.out.print("RECEBI: ");
            MSG_interface.printMSG(receivedPacket);
            ACK ack = new ACK(receivedPacket, port, socket, clientIP, seqPedido);
            ack.send();
            byte[] data = MSG_interface.getDataMsg(receivedPacket);
            file.add(data);
          } else {
            fileReceved = true;
            break;
          }
        } catch (SocketTimeoutException e){
          //System.out.println("acabei");
          //return;
          continue;
        }
      }
    }
    if (!file.isEmpty()){
      writeFile(file);
    }
  }

  //return 0 se sucesso
  public int readFile() {
    this.fileInBytes = new LinkedList<>();
    FileInputStream in;
    try {
      in = new FileInputStream(dir+ '/' + fileName);
    } catch (FileNotFoundException e){
      System.out.println("File not found Exception->  "+ dir+'/'+fileName+"|");
      System.out.println(e.getMessage());
      return -1;
    }
    int size = Constantes.CONFIG.TAIL_SIZE;
    byte[] buff = new byte[size];
    int sizeRead=0;
    try {
      while (-1 != (sizeRead =in.read(buff))){
        for(;sizeRead<size;sizeRead++)
          buff[sizeRead] = (byte) 0;
        fileInBytes.add(buff.clone());
        buff = new byte[size];
      }
    } catch (IOException e) {
      e.printStackTrace();
      return -1;
    }
    return 0;
  }

  public static String toString(DatagramPacket packet){
    byte[] msg = packet.getData();
    return  "[SEND] -> SEQ: "+ msg[1] + "; SEG: " +msg[2] + "; MSG (dados do ficheiro)";
        // + new String(MSG_interface.getDataMsg(packet),StandardCharsets.UTF_8);
  }
}
