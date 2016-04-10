package com.coldcore.coloradoftp.connection;

/**
 * Initiates a data connection with user.
 *
 * This class establishes a new connection to user's machine as a result of a PORT command.
 * This class is a part of a control connection and is created on a per user basis.
 *
 * When a user wants a server to establish a data connection with his/her machine, this class
 * is executed by a control connection untill a data connection is established or failed.
 * In the latter case it is a responsibility of this class to send a failed reply to the user
 * (this will also clear the INTERRUPT state of the control connection).
 * Otherwise, if data connection is established, this class must add it to a connection pool
 * and set control connection's reference to the new data connection.
 *
 * This class can establish connection with user only after user gets "150" reply from the
 * control connection and not before. Data port listener and data connection do not have to
 * check for "150" reply, but this class has to.
 *
 * The easiest way to test this condition is:
 * 1. Before producing "150" relpy, a command saves into a user session amount of bytes
 *    the control connection has sent to the user so far (byte marker)
 * 2. This class must wait until byte marker apperas in the session and the control
 *    connection writes beyond it
 * 3. When control connection clears its outgoing buffer the "150" reply is sent to the user
 * NB! This class may or may not remove byte marker from the session later, so it is the
 *     responsibility of a file command to clear the byte marker before initiating a new
 *     data connection to make sure that this class will wait for a proper "150" reply.
 *
 *
 * ColoradoFTP - The Open Source FTP Server (http://cftp.coldcore.com)
 */

/**数据连接建立对象,这个对象是属于一个控制连接的
　 1.必须当用户接到150回应以后再建立连接*/
public interface DataConnectionInitiator {

  /** Set IP address to connect to
   * @param ip User's IP address
   */
  /**设置需要连接的IP地址
　　　　 ＠param 用户对应的IP地址*/
  public void setIp(String ip);


  /** Get IP address to connect to
   * @return User's IP address
   */
  /**获取用户的IP地址
   @Return 用户的IP地址*/

  public String getIp();

  /** Set port number to connect to
   * @param port User's port number
   */
/**设置要连接到的用户端口
 @param */
  public void setPort(int port);


  /** Get port number to connect to
   * @return User's port number
   */
/**返回连接到的用户端口
    ＠Return　用户端口*/
  public int getPort();


  /** Test if data connection initiator is active and should be executed by a control connection.
   * @return TRUE if it is active, FALSE otherwise
   */
  /**测试数据连接初始化对象是否处于active状态
　　　　　@Return 如果是active状态则返回True否则返回False*/
  public boolean isActive();


  /** Activate (start connecting) */
  /**设置为active状态，即开始进行连接*/
  public void activate();


  /** Abort(中止) data connection initializer (if active then stop connection attempts and send failed reply).
   * This method has nothing to do with ABOR command and reply must not be to ABOR command.
   */
  /**终止数据连接初始化过程*/
  public void abort();


  /** Get control connection of this data connection initiator
   * @return Control connection
   */
 /**返回其所属的控制连接对象
　　　@Return 控制连接对象*/
  public ControlConnection getControlConnection();


  /** Set control connection of this control connection initiator
   * @param controlConnection Control connection
   */
  /**设置该对象所属的控制连接对象
　　　　　@param */

  public void setControlConnection(ControlConnection controlConnection);
}
