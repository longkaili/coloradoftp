/**
 * Command PWD.
 * See FTP spec for details on the command.
 */
package com.coldcore.coloradoftp.command.impl.ftp;

import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.command.impl.AbstractCommand;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;
import com.coldcore.coloradoftp.filesystem.FileSystem;

/**
　　"打印工作目录(PWD)"指令，该指令在回应中返回当前工作目录名．
*/
public class PwdCommand extends AbstractCommand {

   //直接从文件系统取得当前用户的工作目录，然后返回给用户
  public Reply execute() {
    Reply reply = getReply();
    if (!testLogin()) return reply;

    FileSystem fileSystem = (FileSystem) ObjectFactory.getObject(ObjectName.FILESYSTEM);
    String dir = fileSystem.getCurrentDirectory(controlConnection.getSession());

    dir = dir.replaceAll("\"", "\"\""); //Encode double-quated

    reply.setCode("257");
    reply.setText("\""+dir+"\" is current directory.");
    return reply;
  }
}
