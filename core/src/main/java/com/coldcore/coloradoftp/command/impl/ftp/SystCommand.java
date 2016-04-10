/**
 * Command SYST.
 * See FTP spec for details on the command.
 */
package com.coldcore.coloradoftp.command.impl.ftp;

import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.command.impl.AbstractCommand;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;

/**
  "系统(SYST)"指令，该指令返回当前操作系统的类型
*/
public class SystCommand extends AbstractCommand {

  protected String type;


  public SystCommand(String type) {
    super();
    this.type = type;
  }


  public Reply execute() {
    Reply reply = getReply();
    reply.setCode("215");
    reply.setText(type+" system type.");
    return reply;
  }
}
