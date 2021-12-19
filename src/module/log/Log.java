package module.log;

import module.commom.Pair;
import module.status.Information;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Log implements Runnable, Closeable {
  private final Queue<Pair<Boolean,String>> queue;
  private final ReentrantLock l;
  private final Condition c;
  private final BufferedWriter fileLog;
  private final ReentrantLock lFileLog;
  private final Information status;

  public Log(String path, Information information) throws IOException {
    queue = new LinkedList<>();
    l = new ReentrantLock();
    c = l.newCondition();
    lFileLog = new ReentrantLock();
    fileLog = new BufferedWriter(new FileWriter(path));
    status = information;
  }

  private void writeLine(Pair<Boolean,String> pair) throws IOException {
    if (pair.getFirst())
      try {
        lFileLog.lock();
        fileLog.write(pair.getSecond());
        fileLog.write("\n");
        fileLog.flush();
      } finally {
        lFileLog.unlock();
      }
  }

  private boolean isEmpty() {
    try {
      l.lock();
      return queue.isEmpty();
    } finally {
      l.unlock();
    }
  }

  private Pair<Boolean,String> removeQueue() {
    try {
      l.lock();
      return queue.remove();
    } finally {
      l.unlock();
    }
  }

  private void writeQueue() {
    while (!status.isTerminated()) {
        while (!isEmpty()) {
          try {
            writeLine(removeQueue());
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        try {
          l.lock();
          c.await();
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          l.unlock();
        }
    }
  }

  public void addQueueSend(String string) {
    try {
      l.lock();
      queue.add( new Pair<>(true,string));
      c.signalAll();
    } finally {
      l.unlock();
    }
  }

  @Override
  public void run() {
    writeQueue();
    try {
      close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void close() throws IOException {
    try {
      l.lock();
      fileLog.flush();
      fileLog.close();
    } finally {
      l.unlock();
    }
  }
}
