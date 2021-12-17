package module;

import control.SeqPedido;

import java.util.concurrent.locks.ReentrantLock;

public class Information {
  private ReentrantLock l;
  private boolean terminated;

  public Information() {
    this.l = new ReentrantLock();
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
    try {
      l.lock();
      return this.terminated;
    } finally {
      l.unlock();
    }
  }
}
