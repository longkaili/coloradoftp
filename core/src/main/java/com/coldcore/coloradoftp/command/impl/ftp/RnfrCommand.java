/**
 * Command RNFR.
 * See FTP spec for details on the command.
 */
package com.coldcore.coloradoftp.command.impl.ftp;

import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.command.impl.AbstractCommand;
import com.coldcore.coloradoftp.session.Session;
import org.apache.log4j.Logger;

/**
　　"重命名(RNFR)"指令，该指令制定需要被改名的文件的旧路径名．
　　此指令必须紧跟一个"重命名为"指令来指明新的文件名．
*/
public class RnfrCommand extends AbstractCommand {

  private static Logger log = Logger.getLogger(RnfrCommand.class);


 //其实只是简单的将需要重命名的文件路径写入到用户session中去
  public Reply execute() {
    Reply reply = getReply();
    if (!testLogin()) return reply;

    Session session = controlConnection.getSession();
    session.removeAttribute("rnfr.path");

    String path = getParameter();
    if (path.equals("")) {
      reply.setCode("501");
      reply.setText("Send path name.");
      return reply;
    }

    session.setAttribute("rnfr.path", path);

    reply.setCode("350");
    reply.setText("Send RNTO to complete rename.");
    return reply;
  }
}
