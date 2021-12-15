package control;

import java.util.concurrent.locks.ReentrantLock;

public class SeqPedido {
  private byte seq;
  private ReentrantLock l;

  public SeqPedido() {
    seq = (byte) 0;
    l = new ReentrantLock();
  }

  public SeqPedido(byte seqPedido) {
    seq = seqPedido;
    l = new ReentrantLock();
  }

  public byte getSeq() {
    byte r;
    try {
      l.lock();
      r = seq;
      seq++;
    } finally {
      l.unlock();
    }
    return r;
  }
}
