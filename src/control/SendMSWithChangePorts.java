package control;
import java.io.IOException;

public class SendMSWithChangePorts implements Runnable{
  ControlMsgWithChangePorts controlMsgWithChangePorts;

  public SendMSWithChangePorts(ControlMsgWithChangePorts controlMsgWithChangePorts) {
    this.controlMsgWithChangePorts = controlMsgWithChangePorts;
  }

  public void sendFirst() throws IOException {
    controlMsgWithChangePorts.sendFirst();
  }

  @Override
  public void run() {
    controlMsgWithChangePorts.run();
  }
}
