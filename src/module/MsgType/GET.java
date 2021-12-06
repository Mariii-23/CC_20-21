package module.MsgType;

import module.Constantes;
import module.Exceptions.AckErrorException;
import module.Exceptions.PackageErrorException;
import module.Exceptions.TimeOutMsgException;
import module.MSG_interface;
import module.Status.FileStruct;
import module.Type;

import java.io.*;
import static java.nio.file.StandardOpenOption.*;

import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

//TODO mudar o nome este é o send file
public class GET implements MSG_interface {

    String dir;
    private int port;
    InetAddress clientIP;

    DatagramSocket socket;

    Byte seq;
    Byte seqPedido;


    Type type = Type.Get;

    Optional<FileStruct> file;
    String fileName;

    Queue<byte[]> fileInBytes = null;

  public GET(InetAddress clientIp, int port,DatagramSocket socket, Byte seqPedido, FileStruct file,String dir) {
    this.seq = (byte) 0;
    this.port = port;
    this.clientIP = clientIp;
    this.socket = socket;
    this.seqPedido = seqPedido;
    this.file = Optional.ofNullable(file);
    this.fileName = file.getName();
    this.dir = dir;
  }

  public GET(InetAddress clientIP,int port,DatagramSocket socket, byte seq, String fileName, String dir){
    this.seq = (byte) 0;
    this.dir = dir;
    this.port = port;
    this.clientIP = clientIP;
    this.socket = socket;
    this.seqPedido = seq;
    this.fileName = fileName;
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
    return msg[0] == Type.Get.getBytes();
  }

  @Override
  public void createTailPacket(byte[] buff) {
    if (fileInBytes.isEmpty()) return;

    byte[] info = fileInBytes.remove();
    int i2=0;
    int i = Constantes.CONFIG.HEAD_SIZE - 1;
    //System.out.println(Constantes.CONFIG.BUFFER_SIZE + "  BUFFER ->   "+ info.length);

    for (; i2 < info.length && i < Constantes.CONFIG.BUFFER_SIZE ; i++,i2++ ){
      buff[i] = info[i2];
    }
    //System.out.println("@@@@@@@@@@ CRIAR CAUDAAA @@@@@@@@@@@@");
    //System.out.println(new String(buff));
    //System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@");
  }

  public DatagramPacket createPacket() {
    byte[] msg = createMsg(seqPedido,seq); seq++;
    //System.out.println(seq.intValue()+ "ok ok::::");
    //System.out.println("@@@@@@@@@  BUFFER no FINAL  @@@@@@@@");
    //System.out.println(new String(msg));
    //System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@");
    return new DatagramPacket(msg, msg.length, clientIP ,port);
  }


  public Queue<DatagramPacket> createPackets() {
    if (readFile() != 0) return null;
    Queue<DatagramPacket> list = new LinkedList<>();
    //System.out.println("numero de pacotes: "+list.size()  + "numero do array de bytes"+ fileInBytes.size());
    int len = fileInBytes.size();
    for (var i =0 ; i < len  ;i++){
      System.out.println("Criar pacote "+i +"\n\n\n");
      var packet = createPacket();
      //System.out.println(new String(MSG_interface.getDataMsg(packet)));
      //System.out.println("@@@@@@@@@@@@@@@@@@@@");
      list.add(packet);
      //System.out.println("acabadao\n\n\n\n");
      //if (packet!=null)
      //  while (!list.add(packet));
    }
    // falta o utlimo
    //type.flagOn();
    //var packet = createPacket();
    //if (packet!=null)
    //  list.add(packet);

    //System.out.println("numero de pacotes: "+list.size()  + "numero do array de bytes"+ fileInBytes.size());
    return list;
  }

  @Override
  public void send() throws IOException, PackageErrorException {
    //TODO
    // envia o pedido do file que quer
    Queue<DatagramPacket> packets = createPackets();
    System.out.println("numero de pacotes: "+packets.size());
    for(var elem = packets.remove(); elem !=null; elem = packets.remove()){
    //for(var elem: packets){
      //System.out.println(new String(MSG_interface.getDataMsg(elem)));
      //System.out.println("@@@@@@@@@@@@@@@@@@@@@");
      socket.send(elem);
      ACK ack = new ACK(elem,port,socket,clientIP,seqPedido); seqPedido++;
      boolean ackFail = false;
      while (!ackFail) {
        try {
          ack.received();
          ackFail = true;
        } catch (TimeOutMsgException e) {
          // TODO controlo de fluxo
          // vamos diminuindo o tempo de receber cenas
          continue;
        } catch (PackageErrorException e1) {
          // TODO controlo de fluxo
          // a partir de x pacotes errados, fechamos a conecao
          continue;
        } catch (AckErrorException e2) {
          socket.send(elem);
        }
      }
    }
  }

    public void writeFile(Queue<byte[]> array) throws IOException {
    Path p = Path.of(dir+ '/' + fileName);
    System.out.println(p.toString());



    try (
        OutputStreamWriter out  =
            new OutputStreamWriter(Files.newOutputStream(p,CREATE,WRITE,TRUNCATE_EXISTING)))
        //OutputStream out = new BufferedOutputStream(
        //  Files.newOutputStream(p, CREATE, WRITE, TRUNCATE_EXISTING)))
    {

      for(var data : array){
        //String s = new String(data, StandardCharsets.UTF_8);
        String s = new String(data);
        out.write(s,0,s.length());
        //out.write(data,0,data.length);
      }
      out.flush();
      out.close();
    } catch (IOException x) {
      System.err.println(x);
    }
  }

  @Override
  public void received() throws IOException, TimeOutMsgException, PackageErrorException, AckErrorException {
    //TODO
    // recebe o pedido
    boolean fileReceved = false; // so passa a true no ultimo caso
    boolean segFileReceved = false; // so passa a true no ultimo caso

    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    DatagramPacket receivedPacket = new DatagramPacket(buff, Constantes.CONFIG.BUFFER_SIZE);
    Queue<byte[]> file = new LinkedList<>();
    int i =0;
    while (!fileReceved) {
      //file = new LinkedList<>();
      segFileReceved = false;
      while (!segFileReceved){
        try {
          socket.receive(receivedPacket);
          i++;
          segFileReceved = validType(receivedPacket);
          if (segFileReceved) {
            ACK ack = new ACK(receivedPacket, port, socket, clientIP, seqPedido);
            ack.send();

            System.out.println("RECEBI: "+i);
            byte[] data = MSG_interface.getDataMsg(receivedPacket);
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            System.out.println(new String(data));
            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

            file.add(data);
          } else {
            fileReceved = true; //TODO MUDAR
            // aqui é so pra teste, quando ele receber um tipo diff presume que ja nao vai receber mais pacotes
            break;
          }
        } catch (SocketTimeoutException e){
          //System.out.println("acabei");
          //return;
          continue;
        }
      }
    }
    System.out.println("Cheguei aqui");
    if (!file.isEmpty()){
      writeFile(file);
    }
    System.out.println("escrevi num file");

    // envia o file que foi pedido
  }

  //TODO eliminar, apenas é uma demosntracao
  public static void main(String[] args) throws SocketException {
    FileStruct file = new FileStruct(new File("ola"));
    //FileStruct file = new FileStruct(new File("/home/mari/ola"));
    DatagramSocket  s = new DatagramSocket();
    GET getMsg = new GET(s.getLocalAddress(),3001,s, (byte) 0,file,"/home/mari");

    getMsg.readFile();
    //for (var elem : getMsg.fileInBytes){
    //  var s1 = new String(elem);
    //  System.out.println("size "+ elem.length );
    //  System.out.println(s1);
    //  System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
    //}

    System.out.println("PACOTES\n\n\n");
    var pacotes = getMsg.createPackets();
    //for(var elem : pacotes){
    //  byte[] msg = MSG_interface.getDataMsg(elem);
    //  System.out.println(new String(msg));
    //  System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    //}

    //Path p = Path.of("/home/mari/ola2");
    //try (OutputStream out = new BufferedOutputStream(
    //  Files.newOutputStream(p, CREATE, WRITE, TRUNCATE_EXISTING)))
    //{
    //for(var data : getMsg.fileInBytes)
    //  out.write(data, 0, data.length);
    //} catch (IOException x) {
    //System.err.println(x);
    //}
  }

  //return 0 se sucesso
  public int readFile() {

    if (file.isEmpty()) return -1;

    this.fileInBytes = new LinkedList<>();
    FileStruct f = file.get();
    FileInputStream in;
    try {
      in = new FileInputStream(dir+'/'+f.getName());
    } catch (FileNotFoundException e){
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
      System.out.println(e.toString());
      return -1;
    }
    return 0;
  }
}
