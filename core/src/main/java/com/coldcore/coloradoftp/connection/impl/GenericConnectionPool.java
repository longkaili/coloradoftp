package com.coldcore.coloradoftp.connection.impl;

import com.coldcore.coloradoftp.connection.Connection;
import com.coldcore.coloradoftp.connection.ConnectionPool;
import com.coldcore.coloradoftp.connection.TerminatedException;
import com.coldcore.coloradoftp.core.Core;
import com.coldcore.coloradoftp.core.CoreStatus;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @see com.coldcore.coloradoftp.connection.ConnectionPool
 *
 * This class is thread safe as it takes care of all synchronizations.
 */
/**一个通用的线程池类，启动一个单独的线程来管理这个线程池*/
public class GenericConnectionPool implements ConnectionPool, Runnable {

  private static Logger log = Logger.getLogger(GenericConnectionPool.class);
  protected Set<Connection> connections; //该线程集合管理的连接集合
  protected Core core; //FTP服务器对象
  protected Thread thr; //该线程池对应的线程对象
  protected long sleep; //线程睡眠的时间
  protected boolean running; //线程是否处于运行状态


  //默认线程睡眠时间是100毫秒
  public GenericConnectionPool() {
    sleep = 1000L;
  }


  /** Get thread sleep time
   * @return Time in mills
   */
  //获取线程睡眠时间
  public long getSleep() {
    return sleep;
  }


  /** Set thread sleep time
   * @param sleep Time in mills
   */
 //设置线程的睡眠时间
  public void setSleep(long sleep) {
    this.sleep = sleep;
  }

  //初始化，包括初始化连接池集合connections和获得FTP服务器对象
  //并且如果当前没有线程在管理该连接池，则启动一个管理线程
  public void initialize() throws Exception {
    if (connections == null) connections = new HashSet<Connection>();
    core = (Core) ObjectFactory.getObject(ObjectName.CORE);

    //Start this class
    if (!running) {
      running = true;
      thr = new Thread(this);
      thr.start();
    }
  }


 //向连接池中添加一个连接
  public void add(Connection connection) {
    synchronized (connections) {
      connections.add(connection);
    }
  }

  //从连接池中删除一个连接
  public void remove(Connection connection) {
    synchronized (connections) {
      connections.remove(connection);
    }
  }


   //返回连接池的大小
  public int size() {
    synchronized (connections) {
      return connections.size();
    }
  }


  //线程执行体
  public void run() {
   //这个方法体是周期性执行的
   //可以认为run中连接池的管理工作是周期性工作
    while (running) {

    //1.负责检查每个连接的状态
      // 2.负责根据服务器的状态改变连接的状态
      //3.执行连接对象的服务程序
      synchronized (connections) {
        Connection connection = null;
        Iterator<Connection> it = connections.iterator();
        while (it.hasNext())
          try {
            connection = it.next();

            //Remove if destroyed
            if (connection.isDestroyed()) {
              it.remove();
              continue;
            }

            //If a server is poisoned then spread the poison to all its connections
            if (core.getStatus() == CoreStatus.POISONED && !connection.isPoisoned()) {
              connection.poison();
            }

            //Service connection
            connection.service();

          } catch (TerminatedException e) {
            //Normal termination (exception has a message)
            log.debug(e);
            try {
              connection.destroy();
            } catch (Throwable ex) {}

          } catch (Throwable e) {
            //Failed connection
            log.error("Connection failed", e);
            try {
              connection.destroy();
            } catch (Throwable ex) {}
          }
      }

      try {
        Thread.sleep(sleep);
      } catch (Throwable e) {}

    }
    log.debug("Connection pool thread finished");
  }


 //断开整个连接池中所有的连接
  public void destroy() {
    running = false;

    synchronized (connections) {
      for (Connection connection : connections)
        try {
          connection.destroy(); //分别调用每个连接的destroy()方法
        } catch (Throwable e) {}

      connections.clear();
    }

    //Wait for this class to stop (just in case)
    try {
      thr.join(30000);
    } catch (Throwable e) {}
  }


  public Set<Connection> list() {
    synchronized (connections) {
      return new HashSet<Connection>(connections);
    }
  }
}
