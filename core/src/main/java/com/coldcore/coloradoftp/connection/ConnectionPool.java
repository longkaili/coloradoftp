package com.coldcore.coloradoftp.connection;

import java.util.Set;

/**
 * Pool of active connections.
 *
 * All connections should be added to connection pools. Connection pools process connections'
 * self-service routines(日常活动). If a connection fails to be processed (e.g. throws an exception),
 * the pool must call connection's destroy method and then removes it from its list.
 *
 * Connection pools are not allowed to process destroyed connections but may add or return them
 * in appropriate methods.
 *
 * This class must be aware of core's POISONED status. When core is poisoned it is the
 * responsibility of a connection pool to poison all connections in it, so they will die
 * soon and server will shut down.
 *
 *
 * ColoradoFTP - The Open Source FTP Server (http://cftp.coldcore.com)
 */
/**连接管理池对象,是数据连接池和控制连接池的父类。所有的数据连接和控制连接都会被相应的连接池管理，都会被加入这个连接池中
然后由这个连接池对象来控制运行；但是对于已经断开的连接对象，连接池必须要将其清理出去；
如果服务器处于poisioned状态，那么连接池就必须马上将其中的所有连接置成poisoned状态，以关闭服务器*/
public interface ConnectionPool {

  /** Add a connection to the pool
   * @param connection Connection
   */
 /**将一个连接加入到连接池中*/
  public void add(Connection connection);


  /** Remove a connection from the pool
   * @param connection Connection
   */
/**将一个连接从连接池中移除掉*/
  public void remove(Connection connection);


  /** Pool size
   * @return Number of connection in the pool
   */
 /**返回连接池中所包含的连接数量*/
  public int size();


  /** Destroy the pool and kill all connections */
  /**关闭连接池并关闭连接池中的所有连接*/
  public void destroy();


  /** Initialize the pool */
  /**初始化连接池*/
  public void initialize() throws Exception;


  /** List all connections (must return a copy and not the original set)
   * @return Copy of the original set of connections
   */
  /**返回所有连接组成的Set(返回的是一个副本，而不是原来的那些connections)*/
  public Set<Connection> list();
}
