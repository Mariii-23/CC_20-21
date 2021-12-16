package module;

import control.ControlMsgWithChangePorts;

import java.io.IOException;

public class SendMSGwithChangePorts implements Runnable{
  ControlMsgWithChangePorts controlMsgWithChangePorts;

  public SendMSGwithChangePorts(ControlMsgWithChangePorts controlMsgWithChangePorts) {
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
