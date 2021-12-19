package control;

import module.sendAndReceivedMsg.ControlMsgWithChangePorts;

import java.io.IOException;

public class SendMSWithChangePorts implements Runnable {
  private final ControlMsgWithChangePorts controlMsgWithChangePorts;

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
