package com.coldcore.coloradoftp.command;

import com.coldcore.coloradoftp.connection.ControlConnection;

/**
 * User command.
 *
 * This class is executed by a command processor and returns a reply which
 * if then send back to user.
 *
 * In general there should be a separate implementation for every FTP command,
 * so every implementation knows how to handle one (and only one) FTP command.
 * This way commands can be easily replaced later.
 *
 * Control connection can enter an INTERRUPT state. In INTERRUPT state user must
 * wait for a server reply and is not allowed to send anything in but the INTERRUPT
 * commands. As a result, there are two types of FTP commands: one will be processed
 * during the INTERRUPT state and the other will be dropped when a control connection
 * is in that state.
 *
 * Some FTP commands put control connection into the INTERRUPT state which is then
 * cleared when the connection sends a reply. Such reply must refer to a command which
 * is allowed to clear the state or does not have a reference to a command.
 *
 * There is a certain set of commands that must be processed during the INTERRUPT state:
 * ABOR, QUIT and STAT.
 * And usually only one command is allowed to clear that state: ABOR.
 * The rest of FTP commands should not bother with INTERRUPT state.
 *
 *
 * ColoradoFTP - The Open Source FTP Server (http://cftp.coldcore.com)
 */

/**代表FTP命令的类
　　FTP命令分为两类，一类是可以在控制连接处于INTERRUPT状态下可以执行，一类是不能在在控制连接处于INTERRUPT状态下执行的
  所有具体的FTP命令类都会继承这个类；这个类的对象由command processor负责执行,并返回执行结果
  
　*/
public interface Command {

  /** Test if this is command must be processed while a connection is in the INTERRUPT state
   * @return TRUE is it can be, FALSE otherwise
   */
   /**测试命令是否一定要执行在INTERRUPT状态下*/
  public boolean processInInterruptState();


  /** Test if reply to this command must clear INTERRUPT state of a connection
   * @return TRUE is it can, FALSE otherwise
   */
  /**测试这个命令是否一定会清除控制连接的INTRRUPT状态*/
  public boolean canClearInterruptState();


  /** Get name of the command
   * @return Command name
   */
 //取得用户的名字
  public String getName();


  /** Set name of the command
   * @param name Command name
   */
  //设置命令的名字
  public void setName(String name);


  /** Get parameter of the command
   * @return Command parameter
   */
 //获得命令的参数
  public String getParameter();


  /** Set parameter of the command
   * @param parameter Command parameter
   */
  //设置命令的参数
  public void setParameter(String parameter);


  /** Execute the command
   * @return Reply to the command
   */
  //执行命令，同时获得命令执行的返回结果
  public Reply execute();


  /** Execute the command as part of the parent command (e.g. FEAT/HELP/OPTS)
   * @param parent Parent command
   * @return Reply to the command or NULL to allow the parent to provide the default reply
   */
  //将命令当成父命令的一部分执行
  public Reply executeOnParent(Command parent);


  /** Set control connection that submitted this command
   * @param connection Connection
   */
 //设置提交这个命令的控制连接
  public void setConnection(ControlConnection connection);


  /** Get control connection that submitted this command
   * @return Connection
   */
  //取得提交这个命令的控制连接
  public ControlConnection getConnection();
}
