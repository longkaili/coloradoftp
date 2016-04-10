/**
 * Command CWD.
 * See FTP spec for details on the command.
 */
package com.coldcore.coloradoftp.command.impl.ftp;

import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.command.impl.AbstractCommand;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;
import com.coldcore.coloradoftp.filesystem.FileSystem;
import org.apache.log4j.Logger;

/**
   "改变工作路径(CWD)"指令，参数是指定目录的路径名或其它系统的文件集标志符
　　该指令允许用户在不改变它的登录和账户信息的状态下，为存储或下载文件而改变工作目录或数据集
*/

public class CwdCommand extends AbstractCommand {

  private static Logger log = Logger.getLogger(CwdCommand.class);


  public Reply execute() {
    Reply reply = getReply();
    if (!testLogin()) return reply;//用户未登录

//取得需要跳转到的路径
    String dir = getParameter();
    if (dir.length() == 0) {//路径长度为０，默认跳转成功
      reply.setCode("250");
      reply.setText("CWD command successful.");
      return reply;
    }

//取得文件系统对象，然后使用其changeDirectory()方法改变当前用户的路径
    FileSystem fileSystem = (FileSystem) ObjectFactory.getObject(ObjectName.FILESYSTEM);
    fileSystem.changeDirectory(dir, controlConnection.getSession());

    reply.setCode("250");
    reply.setText("Directory changed.");
    return reply;
  }
}
