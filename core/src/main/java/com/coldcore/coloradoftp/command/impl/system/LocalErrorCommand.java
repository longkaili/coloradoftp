package com.coldcore.coloradoftp.command.impl.system;

import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.command.impl.AbstractCommand;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;

/**
 * "Local error" command.
 * System submits this command into a command processor when there is a system error
 * occurred processing a user command.
 */
/**
　　"Local error"　命令
  运行用户的命令时系统出现错误，则系统运行这个命令取得相应的回复
*/
public class LocalErrorCommand extends AbstractCommand {

  public Reply execute() {
    Reply reply = (Reply) ObjectFactory.getObject(ObjectName.REPLY);
    reply.setCode("451");
    reply.setText("Requested action aborted. Local error in processing.");
    return reply;
  }


  public String getName() {
    return "SYSTEM (LOCAL ERROR)";
  }
}
