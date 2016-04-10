/**
 * Command CDUP.
 * See FTP spec for details on the command.
 */
package com.coldcore.coloradoftp.command.impl.ftp;

import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.command.impl.AbstractCommand;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;
import com.coldcore.coloradoftp.filesystem.FileSystem;
import com.coldcore.coloradoftp.session.Session;
import org.apache.log4j.Logger;

/**
  "回到父目录(CDUP)"，不需要参数
  该指令是CWD的一种特殊情况，它用来简化传输目录树的程序，
　因为各操作系统对父目录命令使用不同的语法
*/
public class CdupCommand extends AbstractCommand {

  private static Logger log = Logger.getLogger(CdupCommand.class);


  public Reply execute() {
    Reply reply = getReply();
    if (!testLogin()) return reply;//未登录直接返回

    Session session = controlConnection.getSession();
    FileSystem fileSystem = (FileSystem) ObjectFactory.getObject(ObjectName.FILESYSTEM);
    String curDir = fileSystem.getCurrentDirectory(session);
//调用文件系统的getParent()得到父目录，然后再用changeDirectory()跳转到该目录
    String cdup = fileSystem.getParent(curDir, session);
    fileSystem.changeDirectory(cdup, session);

    reply.setCode("250");
    reply.setText("Directory changed.");
    return reply;
  }
}
