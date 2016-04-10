package com.coldcore.coloradoftp.connection;

import java.io.IOException;

/**
 * Listens for incoing data connections.
 *
 * This class is constantly listening on a predefined port. When user is using PASV command(被动模式命令), his/her
 * control connection is added to the list in this class. User then initiates a data connection to
 * the port this class listens to. When connection is accepted, this class makes sure that an incoming
 * data connection corresponds to(对应) any of the control connections in the list (by IP address). If such
 * control connection is found in the list the new data connection is assigned to it and the control
 * connection is removed from the list.
 *
 * In order to work correctly the internal(内部) list must not contain control connections from the same host.
 * FTP server must open several ports (about 100) to allow PASV to work correctly, so
 * that control connections from the same host(主机) should not wait for one another.
 *
 * The less ports are open the more insecure(不稳定) FTP server gets. Malitious users can try to bing to all
 * available data ports and if one of them has a connection with the same IP that is waiting, the file
 * will be handed to the user (this is the case when all users are using the same gateway, every
 * connection will then have gateway's IP address).
 *
 * If user tries to open a data connecion which fails for some reason then this class has to send a failed
 * reply to the user (this will also clear the INTERRUPT state of the control connection).
 *
 * This class has to remove destroyed connections from the awaiting list itself. It is not allowed to
 * operate on destroyed connections.
 *
 *
 * ColoradoFTP - The Open Source FTP Server (http://cftp.coldcore.com)
 */

/**数据端口监听类，负责在提前指定的端口进行监听，
如果有数据连接到来，则将其与对应的控制连接对象绑定*/
public interface DataPortListener {

  /** Set binding port
   * @param port Port number
   */
  /**设定需要绑定的端口
     ＠param　端口号*/
  public void setPort(int port);


  /** Get binding port
   * @return Port number
   */
  /**获取已绑定的端口号
　　　　 @Retrun 端口号*/
  public int getPort();


  /** Bing to the port */
  /**绑定到指定的端口*/
  public void bind() throws IOException;


  /** Unbind from the port */
  /**从当前绑定的端口截除绑定*/
  public void unbind() throws IOException;


  /** Test if connector is bound to the port.
   * @return TRUE if it is bound, FALSE otherwise
   */
 /**测试绑定是否成功
　　　　　@Return 如果成功返回True,否则返回False*/
  public boolean isBound();


  /** Add a connection to the list of connections which await incoming data connection from users
   * @param connection Connection
   * @return TRUE if connection was added to the list, FALSE if connection cannot be added (perhaps the same IP is already there)
   */
  /**添加一个等待数据连接的控制连接对象到List中*/
  public boolean addConnection(ControlConnection connection);


  /** Remove connection from the list if it exists in it and send a failed reply to the user
   * @param connection Connection
   * @return TRUE if connection was removed from the list, FALSE if connection was not found
   */
    /**将一个控制连接对象从List中移除掉*/
  public boolean removeConnection(ControlConnection connection);
}
