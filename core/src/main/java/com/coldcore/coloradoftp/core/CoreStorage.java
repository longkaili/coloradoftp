package com.coldcore.coloradoftp.core;

import java.util.Set;

/**
 * Global data storage(存储，仓库).
 *
 * This class exists in a single instance and is used to store and exchange global information.
 * For example it may contain a cache for a filesystem, queue of occured errors or any other
 * arbitrary objects.
 *
 *
 * ColoradoFTP - The Open Source FTP Server (http://cftp.coldcore.com)
 */

/*
　这是一个单例类，用于存储和交换一些全局性的数据
  存储一些全局性的特性数据
**/
public interface CoreStorage {

  /** Set attribute
   * @param key Key
   * @param value Value
   */
  public void setAttribute(String key, Object value);


  /** Get attribute
   * @param key Key
   * @return Attribute value
   */
  public Object getAttribute(String key);


  /** Remove attribute
   * @param key Key
   */
  public void removeAttribute(String key);


  /** Get names of all attributes
   * @return Set with names
   */
  public Set<String> getAttributeNames();
}
