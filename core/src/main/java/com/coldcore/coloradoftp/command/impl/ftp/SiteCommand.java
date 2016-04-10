package com.coldcore.coloradoftp.command.impl.ftp;

import java.util.HashMap;

import com.coldcore.coloradoftp.command.Command;
import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.command.impl.AbstractCommand;

public class SiteCommand extends AbstractCommand {

  static HashMap<String, Command> SITE_MAP;

  public SiteCommand(HashMap<String, Command> cmdMap) {
    SITE_MAP = cmdMap;
  }

  public Reply execute() {
    Reply reply = getReply();
    if (!testLogin()) return reply;

    // get request name
    String argument = getParameter();

    // ensure have params
    if (argument == null || argument.length() == 0) {
      reply.setCode("501");
      reply.setText("Syntax error in parameters or arguments.");
      return reply;
    }

    String siteCommand;
    String siteParm;

    int splitIndex = argument.indexOf(" ");
    if (splitIndex == -1) {
      siteCommand = argument.toUpperCase();
      siteParm = "";
    } else {
      siteCommand = argument.substring(0, splitIndex).toUpperCase();
      siteParm = argument.substring(splitIndex + 1);
    }

    String siteRequest = siteCommand;
    //String siteRequest = "SITE_" + siteCommand;

    Command command = SITE_MAP.get(siteRequest);
    try {
      if (command != null) {
        command.setConnection(getConnection());
        command.setParameter(siteParm);
        return command.execute();
      } else {
        reply.setCode("500");
        reply.setText("SITE command unrecognized - " + siteCommand);
        return reply;
      }
    } catch (RuntimeException ex) {
      reply.setCode("451");
      reply.setText("SITE command aborted - " + ex);
      return reply;
    }

  }

}
