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
import java.nio.file.Files;
import java.nio.file.Path;
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

  public SEND(InetAddress clientIp, int port, DatagramSocket socket, SeqPedido seqPedido, String fileName, String dir,
              Log log) {
    this.log = log;
    this.port = port;
    this.clientIP = clientIp;
    this.socket = socket;
    //this.seqPedido = seqPedido.getSeq();
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
    //this.seqPedido = seq.getSeq();
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

    //int counter = 0;
    //System.out.println("counter: " + counter + "; info size: " + info.length + "; info: " + Arrays.toString(info));

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

  public Queue<DatagramPacket> createPackets() {
    // isto nao devia acontecer
    if (readFile() != 0) return new LinkedList<>();

    Queue<DatagramPacket> list = new LinkedList<>();
    //list.add(file)
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
      //TODO Acrescentar cenas ou mudar
      // mandar um bye
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
          timeOutError++;
          if (timeOutError > 5)
            break;
          // TODO controlo de fluxo
          // vamos diminuindo o tempo de receber cenas
          socket.send(elem);
          log.addQueueSend(MSG_interface.MSGToString(elem));
          //continue;
        } catch (PackageErrorException e1) {
          // TODO controlo de fluxo
          // a partir de x pacotes errados, fechamos a conecao
          if (packageError > 3) break;
          packageError++;
          socket.send(elem);
          log.addQueueSend(MSG_interface.MSGToString(elem));
          //continue;
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

    //TODO MUDAR
    ACK ack = new ACK(null, port, socket, clientIP, seqPedido, log);
    seqPedido++;
    try {
      ack.send();
    } catch (Exception e) {
    }
  }

  public void writeFile(Queue<byte[]> array, long lastModification) throws IOException {
    FileWriter myWriter = new FileWriter(dir + '/' + fileName);

    for (var data : array) {
      //int i =0;
      //for( ; i < data.length && data[i] != (byte) 0 ; i++);
      //byte[] copy = Arrays.copyOfRange(data,0,i);

      //String s = new String(copy, StandardCharsets.UTF_8);
      //String s = new String(data,StandardCharsets.UTF_8);
      String s = new String(data);
      myWriter.write(s);
    }
    myWriter.flush();
    myWriter.close();
    File file = new File(dir + '/' + fileName);
    file.setLastModified(lastModification);
  }

  public void send(DatagramSocket socket) throws IOException, PackageErrorException {
    Queue<DatagramPacket> packets = createPackets();
    if (packets.isEmpty()) {
      System.out.println("Nao mandei nenhum ficheiro");
      //TODO Acrescentar cenas ou mudar
      // mandar um bye
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
          // TODO controlo de fluxo
          // vamos diminuindo o tempo de receber cenas
          socket.send(elem);
          log.addQueueSend(MSG_interface.MSGToString(elem));
          //continue;
        } catch (PackageErrorException e1) {
          // TODO controlo de fluxo
          // a partir de x pacotes errados, fechamos a conecao
          if (packageError > 3) break;
          packageError++;
          socket.send(elem);
          log.addQueueSend(MSG_interface.MSGToString(elem));
          //continue;
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

    //TODO MUDAR
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
  }

  @Override
  public void received() throws IOException, TimeOutMsgException, PackageErrorException, AckErrorException {
    // recebe o pedido
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
            System.out.print("RECEBI: ");
            MSG_interface.printMSG(receivedPacket);
            ack = new ACK(receivedPacket, port, socket, clientIP, controlSeqPedido.getSeq(), log);
            ack.send();
            byte[] data = MSG_interface.getDataMsg(receivedPacket);
            if (first) {
              int i;
              for (i = 0; i < data.length && data[i] != (byte) 0; i++) ;
              lastModification = Long.parseLong(new String(Arrays.copyOfRange(data, 0, i)
                  , StandardCharsets.UTF_8));
              first = false;
              continue;
            }
            int i;
            for (i = 0; i < data.length && data[i] != (byte) 0; i++) ;
            if (last == null || !Arrays.equals(last, data)) {
              file.add(Arrays.copyOfRange(data, 0, i));
              last = data.clone();
            }

          } else {
            fileReceved = true;
            break;
          }
        } catch (SocketTimeoutException e) {
          //System.out.println("acabei");
          //return;
          ack = new ACK(receivedPacket, port, socket, clientIP, controlSeqPedido.getSeq(), log);
          ack.send();
        }
      }
    }
    if (!file.isEmpty()) {
      writeFile(file, lastModification);
    }
  }

  //return 0 se sucesso
  public int readFile() {
    this.fileInBytes = new LinkedList<>();
    FileInputStream in;
    File file;
    try {
      in = new FileInputStream(dir + '/' + fileName);
      file = new File(dir + '/' + fileName);
    } catch (FileNotFoundException e) {
      System.out.println("File not found Exception->  " + dir + '/' + fileName + "|");
      System.out.println(e.getMessage());
      return -1;
    }

    //try {
    //  fileInBytes.add(Long.toString(file.lastModified()).getBytes());
    //  byte[] data = Files.readAllBytes(Path.of(dir+'/'+fileName));
    //  int number = data.length / Constantes.CONFIG.TAIL_SIZE;
    //  int resto = data.length - number * Constantes.CONFIG.TAIL_SIZE;

    //  int i;
    //  for ( i =0; i < number; i++) {
    //    int comeco = i*Constantes.CONFIG.TAIL_SIZE;
    //    int fim = (i+1)*Constantes.CONFIG.TAIL_SIZE;
    //    fileInBytes.add(Arrays.copyOfRange(data,comeco,fim));
    //  }

    //  if (resto > 0 ) {
    //    int comeco = i*Constantes.CONFIG.TAIL_SIZE;
    //    int fim = comeco + resto;
    //    fileInBytes.add(Arrays.copyOfRange(data,comeco,fim));
    //  }

    //} catch (IOException e) {
    //  e.printStackTrace();
    //}

    int size = Constantes.CONFIG.TAIL_SIZE;
    byte[] buff = new byte[size];
    //fileInBytes.add(Long.toString(file.lastModified()).getBytes(StandardCharsets.UTF_8));
    fileInBytes.add(Long.toString(file.lastModified()).getBytes());

    int sizeRead = 0;
    try {
      while (-1 != (sizeRead = in.read(buff))) {
        for (; sizeRead < size; sizeRead++)
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

  public static String toString(DatagramPacket packet) {
    byte[] msg = packet.getData();
    //return "[SEND] -> SEQ: " + msg[1] + "; SEG: " + msg[2] + "; MSG (dados do ficheiro)";
    return "[SEND] -> SEQ: " + msg[1] + "; SEG: " + msg[2] + "; MSG: "
     + new String(MSG_interface.getDataMsg(packet),StandardCharsets.UTF_8);
  }
}
