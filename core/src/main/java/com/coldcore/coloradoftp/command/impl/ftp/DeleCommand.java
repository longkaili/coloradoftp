/**
 * Command DELE.
 * See FTP spec for details on the command.
 * 
 * This implementation can also be used as RMD command.
 */
package com.coldcore.coloradoftp.command.impl.ftp;

import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.command.impl.AbstractCommand;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;
import com.coldcore.coloradoftp.filesystem.FileSystem;
import org.apache.log4j.Logger;

/**
  "删除(DELE)"指令，该指令从服务器上删除指定的目录或指定路径的文件　
　可以当成"删除目录(RMD)"指令使用．
*/

public class DeleCommand extends AbstractCommand {

  private static Logger log = Logger.getLogger(DeleCommand.class);


//基本逻辑就是从参数中取得路径名(文件名)，然后调用文件系统的deletePath()函数
  public Reply execute() {
    Reply reply = getReply();
    if (!testLogin()) return reply;

    String path = getParameter();
    if (path.length() == 0) {
      reply.setCode("501");
      reply.setText("Send path name.");
      return reply;
    }

    FileSystem fileSystem = (FileSystem) ObjectFactory.getObject(ObjectName.FILESYSTEM);
    fileSystem.deletePath(path, controlConnection.getSession());

    reply.setCode("250");
    reply.setText("Path deleted.");
    return reply;
  }
}
