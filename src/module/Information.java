package module;

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
    return this.terminated;
  }
}
