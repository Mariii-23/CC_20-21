package module;

import control.SeqPedido;

import java.util.concurrent.locks.ReentrantLock;

public class Information {
  private ReentrantLock l;
  private boolean terminated;

  //private SeqPedido seqPedido;

  public Information() {
    this.l = new ReentrantLock();
    //this.seqPedido = new SeqPedido();
    this.terminated = false;
  }

  public void endProgram(){
   try {
     l.lock();
     this.terminated = true;
   } finally {
     l.unlock();
   }
  }

  public boolean isTerminated(){
    return this.terminated;
  }

  //public byte getNexPedido(){
  //  return seqPedido.getSeq();
  //}

  //public void changeBytePedido(byte seq) {
  //  this.seqPedido = new SeqPedido(seq);
  //}
}
