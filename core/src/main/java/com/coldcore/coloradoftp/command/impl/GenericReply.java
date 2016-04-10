package com.coldcore.coloradoftp.command.impl;

import com.coldcore.coloradoftp.command.Command;
import com.coldcore.coloradoftp.command.Reply;

import java.util.StringTokenizer;

/**
 * @see com.coldcore.coloradoftp.command.Command
 */
//一般的回复对象
public class GenericReply implements Reply {

  protected String code;//回复代码
  protected String text;//回复的文本
  protected Command command;//对应的命令


  public String getCode() {
    return code;
  }


  public void setCode(String code) {
    this.code = code;
  }


  public String getText() {
    return text;
  }


  public void setText(String text) {
    this.text = text;
  }


  public Command getCommand() {
    return command;
  }


  public void setCommand(Command command) {
    this.command = command;
  }


//准备回复的字符串
//包括代码和文本的检查组织
  public String prepare() {
     //如果代码为空，则抛出异常
    if (code == null || code.trim().length() != 3) throw new IllegalArgumentException("Invalid code");
    if (text == null) text = "";//文本为空的设定

    StringBuffer sb = new StringBuffer();
    text = text.trim();//去除前后的空格
    code = code.trim();

    //This may be just code
    //仅有代码
    if (text.equals("")) {
      sb.append(code).append("\r\n");
      return sb.toString();
    }

    //Or code with a single text line
    //仅有单行的文本回复
    if (text.indexOf("\r\n") == -1) {
      sb.append(code).append(" ").append(text).append("\r\n");
      return sb.toString();
    }

    //Or multiline text
//多行的文本回复，
    sb.append(code).append("-");
    StringTokenizer st = new StringTokenizer(text, "\n");
    while (st.hasMoreTokens()) {
      String line = st.nextToken().trim();
      if (!st.hasMoreTokens()) sb.append(code);
      sb.append(" ").append(line).append("\r\n");
    }
    return sb.toString();
  }
}
