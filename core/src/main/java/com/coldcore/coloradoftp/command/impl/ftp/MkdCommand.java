/**
 * Command MKD.
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
  "创建目录(MKD)"指令，此指令创建指定路径名的目录(如果是绝对路径)或则是在当前工作目录创建子目录(如果是相对路径)
*/

public class MkdCommand extends AbstractCommand {

  private static Logger log = Logger.getLogger(MkdCommand.class);

 //基本逻辑就是从参数中取得路径名，然后调用文件系统的createDirectory()创建指定的路径
  public Reply execute() {
    Reply reply = getReply();
    if (!testLogin()) return reply;

    String dir = getParameter();
    if (dir.length() == 0) {
      reply.setCode("501");
      reply.setText("Send directory name.");
      return reply;
    }

    FileSystem fileSystem = (FileSystem) ObjectFactory.getObject(ObjectName.FILESYSTEM);
    dir = fileSystem.createDirectory(dir, controlConnection.getSession());

    dir = dir.replaceAll("\"", "\"\""); //Encode double-quated

    reply.setCode("257");
    reply.setText("\""+dir+"\" directory created.");
    return reply;
  }
}
