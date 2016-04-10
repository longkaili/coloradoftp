package com.coldcore.coloradoftp.command;

/**
 * Server reply to user.
 * This class is used by a control connections to reply to users.
 *
 *
 * ColoradoFTP - The Open Source FTP Server (http://cftp.coldcore.com)
 */
/**服务器对用户的回复类*/
public interface Reply {

  /** Set code
   * @param code Code
   */
 //设定回复的信息代码
  public void setCode(String code);


  /** Get code
   * @return code Code
   */
 //取得回复信息的代码
  public String getCode();


  /** Set text (in multiline text lines must be separated by #13#10)
   * @param text Text
   */
  //设定回复的文本
  public void setText(String text);


  /** Get text
   * @return text Text
   */
  //取得回复的文本
  public String getText();


  /** Prepare reply for transfering
   * @return Prepared reply
   */
 //获得准备传输的文本信息
  public String prepare();


  /** Set command that triggered this reply
   * @param command Command
   */
 //设定关联的命令对象
  public void setCommand(Command command);


  /** Get command that triggered this reply
   * @return Command
   */
  public Command getCommand();
}
