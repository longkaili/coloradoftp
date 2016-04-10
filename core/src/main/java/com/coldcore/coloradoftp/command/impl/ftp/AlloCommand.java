/**
 * Command ALLO.
 * See FTP spec for details on the command.
 *
 * This implementation does nothing from the spec.
 */
package com.coldcore.coloradoftp.command.impl.ftp;

import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.command.impl.AbstractCommand;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;

/**
　　"分配(ALLO)"指令，该指令用于在一些主机上为新传输的文件提供足够的存储空间.
   这里不进行实现
*/
public class AlloCommand extends AbstractCommand {

  public Reply execute() {
    Reply reply = getReply();
    reply.setCode("202");
    reply.setText("Command not implemented, superfluous at this site.");
    return reply;
  }
}
