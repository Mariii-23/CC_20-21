package module.MsgType;

import module.Exceptions.AckErrorException;
import module.Exceptions.PackageErrorException;
import module.Exceptions.TimeOutMsgException;
import module.MSG_interface;
import module.Type;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class List implements MSG_interface {

  private int port;

  InetAddress clientIP;
  String path;

  Type type = Type.List;
  DatagramPacket packet; //
  DatagramSocket socket;
  DatagramSocket serverSocket;

  Byte seq;

  public List(int port, InetAddress clientIP, DatagramSocket socket, Byte seq, String path) throws SocketException {
    this.port = port;
    this.clientIP = clientIP;
    this.packet = null;
    this.socket = socket;
    this.serverSocket = new DatagramSocket();
    this.seq = seq;
    this.path = path;
  }


  public List(DatagramPacket packet,int port,DatagramSocket socket, byte seq, String path) throws SocketException {
    this.port = port;
    this.clientIP = packet.getAddress();
    this.packet = packet;
    this.socket = socket;
    this.seq = seq;
    this.serverSocket = new DatagramSocket();
    this.path = path;
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
    //TODO verfificar
    var msg = packet.getData();
    return Type.seeType(msg[1]) == type.getBytes();
  }

  @Override
  public void createTailPacket(byte[] buff) {

  }

  @Override
  public DatagramPacket createPacket(byte seq) {
    //TODO
    return null;
  }

  @Override
  public void send() throws IOException, PackageErrorException {
    //TODO

  }

  @Override
  public void received() throws IOException, TimeOutMsgException, PackageErrorException, AckErrorException {
    //TODO
  }
}
