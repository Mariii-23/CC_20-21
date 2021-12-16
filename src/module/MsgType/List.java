package module.MsgType;


import control.ControlMsgWithChangePorts;
import control.SeqPedido;

import module.Constantes;
import module.Exceptions.AckErrorException;
import module.Exceptions.PackageErrorException;
import module.Exceptions.TimeOutMsgException;
import module.MSG_interface;
import module.SendMSGwithChangePorts;
import module.Status.Directory;
import module.Status.FileStruct;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class List implements MSG_interface {

  private int port;

  InetAddress clientIP;
  String path;

  Type type = Type.List;
  DatagramPacket packet; //
  DatagramSocket socket;
  DatagramSocket serverSocket;
  Directory dir;


  SeqPedido controlSeqPedido;
  Byte seqPedido;
  Byte seq = (byte) 0;
  Queue<byte[]> qb;
  Queue<DatagramPacket> packets;


  public List(int port, InetAddress clientIP, DatagramSocket socket, SeqPedido seq, String path) throws SocketException {
    this.port = port;
    this.clientIP = clientIP;
    this.packet = null;
    this.socket = socket;
    this.serverSocket = new DatagramSocket();
    this.controlSeqPedido = seq;
    this.path = path;
    this.dir = new Directory(new File(path));
  }

  public List(DatagramPacket packet,int port,DatagramSocket socket, SeqPedido seq, String path) throws SocketException {
    this.port = port;
    this.clientIP = packet.getAddress();
    this.packet = packet;
    this.socket = socket;
    this.controlSeqPedido = seq;
    this.serverSocket = new DatagramSocket();
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
    for (int p = 0 ; p < buff.length && i < Constantes.CONFIG.BUFFER_SIZE; i++, p++)
      buff[i] = b[p];
    for ( ; i < Constantes.CONFIG.BUFFER_SIZE ; i++)
      buff[i] = (byte) 0;
  }

  public void createPackets() {
    this.seqPedido = controlSeqPedido.getSeq();
    //TODO depois verificar o que acontece quando a pasta nao tem ficheiros
    this.packets = new LinkedList<>();
    for (var i =0; i < qb.size(); i++)
      packets.add(createPacket());
  }

  //@Override
  public DatagramPacket createPacket() {
    byte[] msg = createMsg(seqPedido,seq);
    seq++;
    return new DatagramPacket(msg, msg.length, clientIP ,port);
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
      qb.add(Arrays.copyOfRange(b, i * Constantes.CONFIG.TAIL_SIZE, (i+1) * Constantes.CONFIG.TAIL_SIZE));
    }
  }

  @Override
  public void send() throws IOException, PackageErrorException {
    //TODO
    putData();
    createPackets();
    if (packets.isEmpty()) {
      System.out.println("Nao mandei nenhum ficheiro");
      //TODO Acrescentar cenas ou mudar
      // mandar um bye
      return;
    }

    while(!(packets.isEmpty())){
      DatagramPacket elem = packets.remove();
      //MSG_interface.printMSG(elem);
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
    }

    //TODO MUDAR
    ACK ack = new ACK(null,port,socket,clientIP,seqPedido);
    try {
      ack.send();
    } catch (Exception e){
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
    System.out.println("mandar o 1 list");
    socket.send(elem);

    ACK ack = new ACK(elem,port,socket,clientIP,seqPedido); seqPedido++;
    boolean ackSuccesses = false;
    int ackError=0;
    int packageError=0;
    int timeOutError=0;
    while (!ackSuccesses) {
      try {
        ack.received();
        port = ack.getPort();
        ackSuccesses = true;
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
  }

  @Override
  public void send(DatagramSocket socket) throws IOException, PackageErrorException {
    //TODO
    sendFirst(socket);
    while(!(packets.isEmpty())){
      DatagramPacket elem = packets.remove();
      //MSG_interface.printMSG(elem);
      elem.setPort(port);
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
    }

    //TODO MUDAR
    ACK ack = new ACK(null,port,socket,clientIP,seqPedido);
    try {
      ack.send();
    } catch (Exception e){
    }
  }

  public String toData(byte[] msg) {
    int i;
    for (i = 0; i < msg.length && msg[i] != 0; i++);

    return new String(msg, 0, i,StandardCharsets.UTF_8);
  }

  public HashMap<String, FileStruct> createFileMap(String data) {
    if (data == null || data.isEmpty())
      return null;
    HashMap<String, FileStruct> hf = new HashMap<>();
    String[] meta = data.split(";;");
    for (int i = 0; i < meta.length; i = i+2) {
      FileStruct fs = new FileStruct(meta[i], Long.valueOf(meta[i+1]), false);
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
            ACK ack = new ACK(receivedPacket, port, socket, clientIP, controlSeqPedido.getSeq());
            ack.send();
            byte[] data = MSG_interface.getDataMsg(receivedPacket);
            metadata.add(data);
          } else {
            fileReceved = true;
            break;
          }
        } catch (SocketTimeoutException e){
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
    if (filesToReceive!= null)
      System.out.println("files to receive: " + filesToReceive);
    // TODO pedir os files
    var portaPrincipal = Constantes.CONFIG.PORT_UDP;
    LinkedList<Thread> threads = new LinkedList<>();
    if (filesToReceive!=null)
    for (var elem: filesToReceive){
        FileStruct file = new FileStruct(new File(elem));
        GET getMsg = new GET(clientIP,portaPrincipal,socket,controlSeqPedido,file,path);
        ControlMsgWithChangePorts msg = new ControlMsgWithChangePorts(controlSeqPedido,getMsg,clientIP,portaPrincipal);
        SendMSGwithChangePorts t = new SendMSGwithChangePorts(msg);
        
        threads.add(new Thread(t));
        t.sendFirst();
    }
    
    for (var elem : threads )
      elem.start();

    for (var elem : threads )
      try {
        elem.join();
      } catch (Exception ignored) {};
    //  FileStruct file = new FileStruct(new File("ola"));
    //  GET getMsg = new GET(clientIP,port,socket,seqPedido,file,pathDir);
    //  ControlMsgWithChangePorts msg = new ControlMsgWithChangePorts(seqPedido,getMsg,clientIP,port);

    //  FileStruct file1 = new FileStruct(new File("text"));
    //  GET getMsg1 = new GET(clientIP,port,socket,seqPedido,file1,pathDir);
    //  ControlMsgWithChangePorts msg1 = new ControlMsgWithChangePorts(seqPedido,getMsg1,clientIP,port);
    //  SendMSGwithChangePorts t1 = new SendMSGwithChangePorts(msg);
    //  SendMSGwithChangePorts t2 = new SendMSGwithChangePorts(msg1);

    //  ////testar mandar file
    //    Thread t[] = new Thread[2];
    //      t[0] = new Thread(t1);
    //      t[1] = new Thread(t2);
    //      t1.sendFirst();
    //      t2.sendFirst();
    //      t[0].start();
    //      t[1].start();
  }

  public static String toString(DatagramPacket packet){
    byte[] msg = packet.getData();
    return  "[List]  -> SEQ:" + msg[1] + "; SEG: " +msg[2] + "; MSG: " //Metadados";
            + new String(MSG_interface.getDataMsg(packet));
  }

  @Override
  public String toString() {
    return toString(packet);
  }
}
