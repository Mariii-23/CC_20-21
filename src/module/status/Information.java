package module.status;

import module.Constantes;
import module.logins.Login;

import java.util.concurrent.locks.ReentrantLock;

public class Information {
  private final ReentrantLock l;
  private boolean terminated;

  private final Login login;
  private final String pathDir;

  public Information(String pathDir) {
    this.pathDir = pathDir;
    this.l = new ReentrantLock();
    this.terminated = false;
    this.login = new Login(pathDir + '/' + Constantes.PATHS.LOGINS );
    login.readAutenticationFile();
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

  public String generateKey() {
    return login.generateKey();
  }

  public boolean validate(String givenKey, String recievedValue) {
    return login.validate(givenKey,recievedValue);
  }

  public String getValue(String key){
    return login.autenticate(key);
  }
}
