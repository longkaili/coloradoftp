package com.coldcore.coloradoftp.connection;

import java.util.Set;

/**
 * Set of data port listeners.
 *
 * Because there can be many data port listeners, an object is required to
 * replicate methods to all, rather than processing each data port listener
 * individualy.
 *
 *
 * ColoradoFTP - The Open Source FTP Server (http://cftp.coldcore.com)
 */
/**数据端口侦听类的集合，因为这里可能会有多个数据端口侦听对象的存在
　　所以用一个集合来对其进行统一的管理*/
public interface DataPortListenerSet {

  /** Bind(绑定) data port listeners
   * @return Number of bound listeners
   */
  /**以绑定的数据端口侦听对象*/
  public int bind();


  /** Unbind data port listeners
   * @return Number of unbound listeners
   */
  /**未绑定的数据端口侦听对象*/
  public int unbind();


  /** Get number of boud listeners
   * @return Number of boud listeners
   */
  /**返回对象总数目*/
  public int boundNumber();


  /** List listeners
   * @return Listeners (copy of the original list)
   */
  /**返回所有对象组成的集合，是原集合的一份拷贝*/
  public Set<DataPortListener> list();
}
