package com.coldcore.coloradoftp.connection;

import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.session.Session;

/**
 * Control connection.
 *
 * When user connects to a server a control connection is created and assigned to(被分配给) him/her.
 * One user may have only one control connection which lives until user disconnects.
 * Control connection accepts user commands, executes them and sends replies.
 *
 * Control connection acts as a main point for all users' operations, every user is
 * identified by a control connection. As a result, control connection has references
 * to all user-related objects and vice versa(反的).
 *
 * Note that a user may also have a single data connection. Which can be created and destroyed
 * as many times as required while the control connection lives on.
 * Control connection has a reference to a data connection initiator which is activated
 * when there is a need to establish a new data connection.
 *
 * Control connection may be poisoned at any time. When poisoned, a connection will commit
 * suicide as soon as all the data is written out to the user and the data connection finishes.
 * This feature is required to drop connections when server is full: when message is written
 * out to a user the connection will be dropped right away. Or when a user sends QUIT command,
 * the control connection will wait until the data connection dies and then it will kill itself.
 * So poisoned status must be set when user submits QUIT command.
 *
 * Some FTP commands put control connection into the INTERRUPT state which is then
 * cleared when the connection sends a reply. Such reply must refer to a command which
 * is allowed to clear the state or does not have a reference to a command.
 * Usually INTERRUPT state is trigerred(最终引发) by data transfer commands and cleared right
 * after data transfer ends.
 *
 *
 * ColoradoFTP - The Open Source FTP Server (http://cftp.coldcore.com)
 */
/**1.控制连接和用户是一一对应的，当一个用户连接到服务器时，一个控制连接就被分配给这个用户
   2.控制连接的生命周期从用户建立连接开始直到用户断开连接为止
   3.控制连接的主要功能是接受用户的命令，执行用户的命令并将执行结果返回给用户
   4.每个用户也只能有一个数据连接，但是数据连接是可以多次断开和重新连接的；控制连接中
　　　会持有对应用户的数据连接对象。
　  5.当用户输入quit命令时，服务器要先进入poisoned状态,然后等待数据传输结束后，断开连接。
   6.有些FTP命令可以将控制连接变成中断状态，但是这种中断必须要在服务器返回应答后就结束。
　　　一般这种中断是由数据传输命令引起的，当数据传输结束后中断也就结束了。
*/

/**控制连接对象，是一个接口，没有具体的实现*/
public interface ControlConnection extends Connection {

  /** Reply to user
   * @param reply Reply
   */
/**向用户做出应答*/
  public void reply(Reply reply);


  /** Get user session
   * @return Session
   */
 /**取得用户的会话信息*/
  public Session getSession();


  /** Get data connection of this control connection
   * @return Data connection or NULL if does not have one
   */
  /**获得该控制连接对应的数据连接*/
  public DataConnection getDataConnection();


  /** Set data connection of this control connection
   * @param dataConnection Data connection
   */
  /**设置该用户控制连接对应的数据连接*/
  public void setDataConnection(DataConnection dataConnection);


  /** Get data connection initiator
   * @return Initiator
   */
 /**获得数据连接初始化*/
  public DataConnectionInitiator getDataConnectionInitiator();


  /** Get text length that awaits in the outgoing to user buffer
   * @return Test length
   */
 /**获得即将发送给用户缓冲区的文本的长度*/
  public int getOutgoingBufferSize();


  /** Get text length that awaits in the incoming from user buffer
   * @return Test length
   */
/**获得从用户缓冲区中得到数据的长度*/
  public int getIncomingBufferSize();
}
