package module;

import module.Exceptions.AckErrorException;
import module.Exceptions.PackageErrorException;
import module.Exceptions.TimeOutMsgException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public interface MSG_interface {
  //InetAddress getAddress(); // endereco a enviar
  int getPort();
  Type getType();



  public boolean validType(DatagramPacket packet);

  public String toString();

  //temos que implementar
  //public static boolean validType(DatagramPacket packet) {
  //  return false;
  //}

  default byte getSeq(DatagramPacket packet) {
    return packet.getData()[1];
  }

  default byte getSeqSegmento(DatagramPacket packet) {
    return packet.getData()[2];
  }

  default void createHeadPacket(byte sed, byte sedSeg, byte[] buff) {
    buff[0] = getType().getBytes();
    buff[1] = sed;
    buff[2] = sedSeg;
  }

  public void createTailPacket(byte[] buff);

  default public byte[] createMsg(byte seq, byte seqSeg){
    byte[] buff = new byte[Constantes.CONFIG.BUFFER_SIZE];
    createHeadPacket(seq, seqSeg, buff);
    createTailPacket(buff);
    return buff;
  }

  public DatagramPacket createPacket(byte seq, byte seqSeg);

  public void send() throws IOException, PackageErrorException;
  public void received() throws IOException, TimeOutMsgException, PackageErrorException, AckErrorException;
}
