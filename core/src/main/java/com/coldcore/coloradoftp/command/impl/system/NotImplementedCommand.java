package com.coldcore.coloradoftp.command.impl.system;

import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.command.impl.AbstractCommand;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;

/**
 * "Not implemented" command.
 * System submits this command instead into a command processor when user submits a command which
 * cannot be found among all available commands - a command which is not implemented.
 */
//当用户提交的任务在所有命令中都找不到时，执行这个命令
public class NotImplementedCommand extends AbstractCommand {

  public Reply execute() {
    Reply reply = (Reply) ObjectFactory.getObject(ObjectName.REPLY);
    reply.setCode("502");
    reply.setText("Command not implemented.");
    return reply;
  }


  public String getName() {
    return "SYSTEM (NOT IMPLEMENTED)";
  }
}
