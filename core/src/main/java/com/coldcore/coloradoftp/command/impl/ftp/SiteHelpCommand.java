package com.coldcore.coloradoftp.command.impl.ftp;

import java.util.HashMap;
import java.util.Map.Entry;

import com.coldcore.coloradoftp.command.Command;
import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.command.impl.AbstractCommand;

/**
　　"站点参数(SITE)"指令，该指令给出服务器可提供的基本文件传输服务
  而不是协议的全部指令
*/
public class SiteHelpCommand extends AbstractCommand {

  private HashMap<String, Command> SITE_MAP = null;
  private StringBuilder sb = null;

  public String getHelpText() {
    return "Displays this message";
  }

  public Reply execute() {
    SITE_MAP = SiteCommand.SITE_MAP;
    sb = new StringBuilder();
    Reply reply = getReply();

    buildMessage("The following SITE commands are supported");
    for (Entry<String, Command> e : SITE_MAP.entrySet()) {
      String cmd = e.getKey();
      AbstractCommand c = (AbstractCommand) e.getValue();
      String cmdHelp = c.getShortHelpText();
      String msg = String.format("%-9s%s", cmd, cmdHelp);
      buildMessage(msg);
    }
    buildMessage("=========================================");

    replyOK(reply, sb);
    return reply;
  }

  void replyOK(Reply reply, StringBuilder sb) {
    reply.setCode("200");
    reply.setText(sb.toString());
  }

  void buildMessage(String txt) {
    sb.append(txt + "\r\n");
  }

}
