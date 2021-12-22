package module.status;

import module.Constantes;
import module.logins.Login;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Information {
  private final ReentrantLock l;
  private boolean terminated;

  private final Login login;
  private final String pathDir;

  private final Set<String> filesToIgnored;
  private final ReentrantLock lFiles;

  private boolean startMenu;
  private final ReentrantLock lStartMenu;
  private final Condition conditionStartMenu;

  public Information(String pathDir) {
    this.pathDir = pathDir;
    this.l = new ReentrantLock();
    this.terminated = false;
    this.login = new Login(pathDir + '/' + Constantes.PATHS.LOGINS);
    login.readAutenticationFile();
    this.filesToIgnored = new HashSet<>();
    this.lFiles = new ReentrantLock();
    initFilesToIgnored();

    this.startMenu = false;
    this.lStartMenu = new ReentrantLock();
    this.conditionStartMenu = lStartMenu.newCondition();
  }

  private void initFilesToIgnored() {
    try {
      lFiles.lock();
      this.filesToIgnored.add(Constantes.PATHS.LOG_NAME_FILE);
      this.filesToIgnored.add(Constantes.PATHS.LOGINS);
      this.filesToIgnored.add(Constantes.PATHS.LOG_Received_NAME_FILE);
      this.filesToIgnored.add(Constantes.PATHS.LOG_Time_NAME_FILE);
    } finally {
      lFiles.unlock();
    }
  }

  public Boolean equalFileToIgnored(String filename) {
    try {
      lFiles.lock();
      return this.filesToIgnored.contains(filename);
    } finally {
      lFiles.unlock();
    }
  }

  public void addFileToIgnored(String filename) {
    try {
      lFiles.lock();
      this.filesToIgnored.add(filename);
    } finally {
      lFiles.unlock();
    }
  }


  public void endProgram() {
    try {
      l.lock();
      this.terminated = true;
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

  public boolean validate(String givenKey, String recievedValue) {
    return login.validate(givenKey, recievedValue);
  }

  public String getValue(String key) {
    return login.autenticate(key);
  }
}
