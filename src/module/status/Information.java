package module.status;

import module.Constantes;
import module.logins.Login;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static module.Constantes.PATHS.*;
import static module.commom.Colors.*;

public class Information {
  private final ReentrantLock l;
  private boolean terminated;

  private final Login login;
  private final String pathDir;

  public final HashSet<String> logFiles = new HashSet<>(Arrays.asList(
      LOGINS,LOG_NAME_FILE, LOG_Received_NAME_FILE, LOG_Time_NAME_FILE));
  private final Set<String> filesToIgnore;
  private final ReentrantLock lFiles;

  private boolean startMenu;
  private final ReentrantLock lStartMenu;
  private final Condition conditionStartMenu;

  private int numberThreads;
  private int numberTotalThreads;
  private final ReentrantLock lNumberThreads;

  private int numberSendFiles;
  private final ReentrantLock lNumberSendFiles;
  private int numberGetFiles;
  private final ReentrantLock lNumberGetFiles;

  public Information(String pathDir) {
    this.pathDir = pathDir;
    this.l = new ReentrantLock();
    this.terminated = false;
    this.login = new Login(pathDir + '/' + LOGINS);
    login.readAuthenticationFile();
    this.filesToIgnore = new HashSet<>();
    this.lFiles = new ReentrantLock();
    initFilesToIgnore();

    this.startMenu = false;
    this.lStartMenu = new ReentrantLock();
    this.conditionStartMenu = lStartMenu.newCondition();

    this.numberThreads = 0;
    this.numberTotalThreads = 0;
    this.lNumberThreads = new ReentrantLock();

    this.numberSendFiles = 0;
    this.lNumberSendFiles = new ReentrantLock();
    this.numberGetFiles = 0;
    this.lNumberGetFiles = new ReentrantLock();
  }


  public void increaseThread() {
    try {
      this.lNumberThreads.lock();
      this.numberThreads++;
      this.numberTotalThreads++;
    } finally {
      this.lNumberThreads.unlock();
    }
  }

  public void decreaseThread() {
    try {
      this.lNumberThreads.lock();
      this.numberThreads++;
    } finally {
      this.lNumberThreads.unlock();
    }
  }


  public void increaseSendFiles() {
    try {
      this.lNumberSendFiles.lock();
      this.numberSendFiles++;
    } finally {
      this.lNumberSendFiles.unlock();
    }
  }

  public int numberSendFiles() {
    try {
      this.lNumberSendFiles.lock();
      return this.numberSendFiles;
    } finally {
      this.lNumberSendFiles.unlock();
    }
  }

  public void increaseGetFiles() {
    try {
      this.lNumberGetFiles.lock();
      this.numberGetFiles++;
    } finally {
      this.lNumberGetFiles.unlock();
    }
  }

  public int numberGetFiles() {
    try {
      this.lNumberGetFiles.lock();
      return this.numberGetFiles;
    } finally {
      this.lNumberGetFiles.unlock();
    }
  }

  private void initFilesToIgnore() {
    try {
      lFiles.lock();
      this.filesToIgnore.add(Constantes.PATHS.LOG_NAME_FILE);
      this.filesToIgnore.add(LOGINS);
      this.filesToIgnore.add(Constantes.PATHS.LOG_Received_NAME_FILE);
      this.filesToIgnore.add(Constantes.PATHS.LOG_Time_NAME_FILE);
    } finally {
      lFiles.unlock();
    }
  }

  public Boolean equalFileToIgnore(String filename) {
    try {
      lFiles.lock();
      return this.filesToIgnore.contains(filename);
    } finally {
      lFiles.unlock();
    }
  }

  public void addFileToIgnored(String filename) {
    try {
      lFiles.lock();
      this.filesToIgnore.add(filename);
    } finally {
      lFiles.unlock();
    }
  }

  public void addFileToSynchronize(String filename) {
    try {
      lFiles.lock();
      this.filesToIgnore.remove(filename);
    } finally {
      lFiles.unlock();
    }
  }

  public String filesToIgnoredToString() {
    var s = new StringBuilder(ANSI_BOLD + ANSI_CYAN + "Files ignored\n" + ANSI_RESET);
    try {
      int i = 1;
      lFiles.lock();
      for(var elem : filesToIgnore){
        s.append(ANSI_CYAN).append(i).append(" -> ").append(ANSI_RESET); i++;
        s.append(elem).append("\n");
      }
    } finally {
      lFiles.unlock();
    }
    return s.toString();
  }

  public void endProgram() {
    try {
      l.lock();
      this.terminated = true;
      System.out.println("Finishing the program...");
    } finally {
      l.unlock();
    }
  }

  public boolean isTerminated() {
    try {
      l.lock();
      return this.terminated;
    } finally {
      l.unlock();
    }
  }

  public void startMenu() {
    try {
      lStartMenu.lock();
      while(!startMenu) {
        try {
          conditionStartMenu.await();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    } finally {
      lStartMenu.unlock();
    }
  }

  public void setStartMenuOn() {
    try {
      lStartMenu.lock();
      startMenu = true;
      conditionStartMenu.signalAll();
    } finally {
      lStartMenu.unlock();
    }
  }

  public String generateKey() {
    return login.generateKey();
  }

  public boolean validate(String givenKey, String receivedValue) {
    return login.validate(givenKey, receivedValue);
  }

  public String getValue(String key) {
    return login.autenticate(key);
  }
}
