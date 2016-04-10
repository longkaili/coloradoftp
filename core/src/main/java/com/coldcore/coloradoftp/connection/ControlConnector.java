package com.coldcore.coloradoftp.connection;

import java.io.IOException;

/**
 * Accepts incoming control connections.
 *
 * When connection is accepted on a predefined(预定义的) port, this class configures it,
 * writes a welcome/reject message into it and adds it to a connection pool which will
 * serve it till the connection dies.
 *
 * Server should not just drop a connection leaving user with no response message. If a
 * connection must be dropped, it should be poisoned instead, this way the connection
 * writes out the message to its user and then dies.
 *
 * This class must be aware of core's POISONED status. When core is poisoned it is the
 * responsibility of a connector to not to allow any more incoming connections, so server
 * can shut down normally.
 *
 *
 * ColoradoFTP - The Open Source FTP Server (http://cftp.coldcore.com)
 */
/**这个类负责在指定的端口上进行侦听，当有用户进行连接时，
向用户发送欢迎/拒绝连接信息，负责配置对应的connection对象，将其加入连接池中*/
public interface ControlConnector {

  /** Bing to the port and start listening for incoming connections */
  /**绑定到指定的端口，并开始进行侦听*/
  public void bind() throws IOException;


  /** Unbind from the port and stop listening for incoming connections */
  /**从指定端口解除绑定，停止侦听*/
  public void unbind() throws IOException;


  /** Test if connector is bound to the port.
   * @return TRUE if it is bound, FALSE otherwise
   */
 /**测试是否已经绑定成功
　　　　　@Return 如果绑定成功返回True,否则返回False
*/
  public boolean isBound();


  /** Set binding port
   * @param port Port number
   */
/**设置绑定的端口
   @param　对应到端口号
*/
  public void setPort(int port);


  /** Get binding port
   * @return Port number
   */
/**获取目前绑定的端口
　　　　　@Return　对应的端口号　　
*/
  public int getPort();
}
