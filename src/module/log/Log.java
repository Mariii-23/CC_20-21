package module.log;

import module.Constantes;
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
  private final Queue<Pair<Integer, String>> queue;
  private final ReentrantLock l;
  private final Condition c;
  private final BufferedWriter fileLog;
  private final ReentrantLock lFileLog;
  private final BufferedWriter fileLogTime;
  private final ReentrantLock lFileLogTime;
  private final BufferedWriter fileLogReceived;
  private final ReentrantLock lFileLogReceived;
  public final Information status;

  public Log(String path, Information information) throws IOException {
    queue = new LinkedList<>();
    l = new ReentrantLock();
    c = l.newCondition();
    lFileLog = new ReentrantLock();
    fileLog = new BufferedWriter(new FileWriter(path + '/' + Constantes.PATHS.LOG_NAME_FILE));
    lFileLogTime = new ReentrantLock();
    fileLogTime = new BufferedWriter(new FileWriter(path + '/' + Constantes.PATHS.LOG_Time_NAME_FILE));
    lFileLogReceived = new ReentrantLock();
    fileLogReceived = new BufferedWriter(new FileWriter(path + '/' + Constantes.PATHS.LOG_Received_NAME_FILE));
    status = information;
  }

  private void writeLineSend(String s) throws IOException {
    try {
      lFileLog.lock();
      fileLog.write(s);
      fileLog.write("\n");
      fileLog.flush();
    } finally {
      lFileLog.unlock();
    }
  }

  private void writeLineTime(String s) throws IOException {
    try {
      lFileLogTime.lock();
      fileLogTime.write(s);
      fileLogTime.write("\n");
      fileLogTime.flush();
    } finally {
      lFileLogTime.unlock();
    }
  }

  private void writeLineReceived(String s) throws IOException {
    try {
      lFileLogReceived.lock();
      fileLogReceived.write(s);
      fileLogReceived.write("\n");
      fileLogReceived.flush();
    } finally {
      lFileLogReceived.unlock();
    }
  }

  private void writeLine(Pair<Integer, String> pair) throws IOException {
    var s = pair.getSecond();
    switch (pair.getFirst()) {
      case 0:
        writeLineSend(s);
        break;
      case 1:
        writeLineTime(s);
        break;
      case 2:
        writeLineReceived(s);
        break;
      default:
        break;
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

  private Pair<Integer, String> removeQueue() {
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
      queue.add(new Pair<>(0, string));
      c.signalAll();
    } finally {
      l.unlock();
    }
  }

  public void addQueueTime(String string) {
    try {
      l.lock();
      queue.add(new Pair<>(1, string));
      c.signalAll();
    } finally {
      l.unlock();
    }
  }

  public void addQueueReceived(String string) {
    try {
      l.lock();
      queue.add(new Pair<>(2, string));
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
