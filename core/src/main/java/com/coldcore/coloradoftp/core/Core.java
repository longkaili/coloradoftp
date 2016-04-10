package com.coldcore.coloradoftp.core;

/**
 * The core itself.
 *
 * Core is responsible for starting/stopping FTP server and all its parts.
 * There is nothing else the core can do because starting from version 1.2
 * all components(组件) run in separate threads and therefore execute on(执行) their own.
 *
 * Core has a special status names POISONED. This status means that the server is going to shut
 * down and is waiting for all control and data connections to finish and disconnect. All connection
 * pools and control connection acceptors must take core status into consideration.
 *
 * When poisoned and there are no connections left to serve, the server will not neccessarily stop.
 * This is up to the implementation of the core.
 *
 *
 * ColoradoFTP - The Open Source FTP Server (http://cftp.coldcore.com)
 */

/* 
   负责启动FTP服务器的接口
   提供了下面几个函数
*/
public interface Core {

  /** Start server */
  /**启动服务器*/
  public void start();


  /** Stop server */
  /**停止服务器*/
  public void stop();


  /** Poison server (no more connections allowed and existing will be killed when all data is transferred) */
  /**抑制服务器,当服务器处于抑制状态时，将不再允许新的连接，同时再将数据传输完后，退出服务器*/
  public void poison();


  /** Get server status
   * @return Server status
   */
  /**返回服务器当前的状态*/
  public CoreStatus getStatus();
}
