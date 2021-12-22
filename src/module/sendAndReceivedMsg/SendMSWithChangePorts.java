package module.sendAndReceivedMsg;

import module.sendAndReceivedMsg.ControlMsgWithChangePorts;
import module.status.Information;

import java.io.IOException;

public class SendMSWithChangePorts implements Runnable {
  private final ControlMsgWithChangePorts controlMsgWithChangePorts;
  private final Information information;

  public SendMSWithChangePorts(ControlMsgWithChangePorts controlMsgWithChangePorts, Information information) {
    this.controlMsgWithChangePorts = controlMsgWithChangePorts;
    this.information = information;
  }

  public void sendFirst() throws IOException {
    controlMsgWithChangePorts.sendFirst();
  }

  @Override
  public void run() {
    information.increaseThread();
    controlMsgWithChangePorts.run();
    information.decreaseThread();
  }
}
