package com.coldcore.coloradoftp.connection.impl;

import com.coldcore.coloradoftp.connection.DataPortListener;
import com.coldcore.coloradoftp.connection.DataPortListenerSet;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * Set of data port listeners.
 */
/**
   数据端口监听对象集合类，集中管理数据端口监听对象
*/
public class GenericDataPortListenerSet implements DataPortListenerSet {

  private static Logger log = Logger.getLogger(GenericDataPortListenerSet.class);
  private Set<DataPortListener> listeners; //数据端口监听对象集合


  public GenericDataPortListenerSet(Set<DataPortListener> listeners) {
    this.listeners = new HashSet<DataPortListener>(listeners);
  }


  //绑定集合中所有数据端口监听对象
  public synchronized int bind() {
    if (listeners.isEmpty()) {
      log.warn("No data port listeners configured, PASV command will be disabled");
      return 0;
    }

    int bound = 0;
    for (DataPortListener listener : listeners)
      try {
        listener.bind();
        bound++;
      } catch (Throwable e) {
        log.error("Cannot bind data port listener on port "+listener.getPort()+" (ignoring)", e);
      }

    if (bound == 0) {
      log.fatal("Could not bind any of data port listeners");
      return 0;
    }

    log.info("Data port listeners bound: "+bound+"/"+listeners.size());
    return bound;
  }


  //将集合中所有数据端口监听对象解除绑定
  public synchronized int unbind() {
    int bound = 0;
    int unbound = 0;
    for (DataPortListener listener : listeners)
      if (listener.isBound())
        try {
          bound++;
          listener.unbind();
          unbound++;
        } catch (Throwable e) {
          log.error("Cannot unbind data port listener on port "+listener.getPort()+" (ignoring)", e);
        }

    log.info("Data port listeners unbound: "+unbound+"/"+bound);
    return unbound;
  }


//返回已绑定的数据端口数目
  public int boundNumber() {
    int bound = 0;
    for (DataPortListener listener : listeners)
      if (listener.isBound()) bound++;
    return bound;
  }


  //返回数据端口监听对象集合的一个拷贝
  public Set<DataPortListener> list() {
    return new HashSet<DataPortListener>(listeners);
  }
}
