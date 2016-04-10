/**
 * @see com.coldcore.coloradoftp.core.Core
 */

/**
   Core接口的具体实现
*/
package com.coldcore.coloradoftp.core.impl;

import com.coldcore.coloradoftp.connection.ConnectionPool;
import com.coldcore.coloradoftp.connection.ControlConnector;
import com.coldcore.coloradoftp.connection.DataPortListenerSet;
import com.coldcore.coloradoftp.core.Core;
import com.coldcore.coloradoftp.core.CoreStatus;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;
import org.apache.log4j.Logger; //apache提供的日志类

public class GenericCore implements Core {

  private static Logger log = Logger.getLogger(GenericCore.class);
  protected CoreStatus status; //记录服务器当前的状态
  protected ControlConnector controlConnector;//控制连接初始化对象
  protected DataPortListenerSet dataPortListenerSet;//数据端口监听对象集合
  protected ConnectionPool controlConnectionPool;//目前存活的控制连接对象的集合
  protected ConnectionPool dataConnectionPool;//目前存活的数据连接对象的集合



  //默认构造器
  //默认状态为STOPPED
  public GenericCore() {
    //默认服务器状态是STOPPED
    status = CoreStatus.STOPPED;
  }



  //启动服务器的方法
  synchronized public void start() {

  //如果不是停止状态则抛出异常
    if (status != CoreStatus.STOPPED) {
      log.warn("Server was running when start routine was submitted");
      throw new IllegalStateException("Stop the server first");
    }

    //Get all required objects
    //使用工厂方法取得启动服务器所需要的所有对象
    controlConnector = (ControlConnector) ObjectFactory.getObject(ObjectName.CONTROL_CONNECTOR);
    dataPortListenerSet = (DataPortListenerSet) ObjectFactory.getObject(ObjectName.DATA_PORT_LISTENER_SET);
    controlConnectionPool = (ConnectionPool) ObjectFactory.getObject(ObjectName.CONTROL_CONNECTION_POOL);
    dataConnectionPool = (ConnectionPool) ObjectFactory.getObject(ObjectName.DATA_CONNECTION_POOL);

    try {
      //Initialize connection pools
    //初始化连接池
      controlConnectionPool.initialize();
      dataConnectionPool.initialize();

      //Bind data port listeners
      //绑定数据端口
      if (!bindDataPortListeners()) throw new Exception("Unable to bind data port listeners");

      //Bind control connector
     //绑定控制连接配置器
      if (!bindControlConnector()) throw new Exception("Unable to bind control connector");

    } catch (Throwable e) {
      //Terminate everything

      boolean noerrors = terminate();

      if (noerrors) log.info("Server terminated");
      else log.warn("Server terminated (with errors)");

      throw new RuntimeException("Cannot start server", e);
    }

 ///更改状态为RUNNING，表示FTP服务器启动成功
    status = CoreStatus.RUNNING;
    log.info("Server started");
  }


  /** Terminate everything
   * @return TRUE if there were no errors, FALSE otherwise
   */
  protected boolean terminate() {
    boolean noerrors = true;
    if (!unbindControlConnector()) noerrors = false;
    if (!unbindDataPortListeners()) noerrors = false;
    dataConnectionPool.destroy();//停止所有的数据连接
    controlConnectionPool.destroy();//停止所有的控制连接

    //Wait a bit (just in case)
    try {
      Thread.sleep(100L);
    } catch (Throwable e) {}

    return noerrors;
  }


  //停止服务器
  synchronized public void stop() {
    if (status == CoreStatus.STOPPED) {
      log.warn("Server was stopped when stop routine was submitted");
      throw new IllegalStateException("Cannot stop the server, it is stopped already");
    }

    //Terminate everything
    boolean noerrors = terminate();

    status = CoreStatus.STOPPED;
    if (noerrors) log.info("Server stopped");
    else log.warn("Server stopped (with errors)");

    if (!noerrors) throw new RuntimeException("Abnormal server termination");
  }


 //设置服务器的状态为待结束的状态
  public void poison() {
    //Setting status to poisoned should tell connection pools and control connector to poison all connections
    status = CoreStatus.POISONED;
  }


  //返回服务器的状态
  public CoreStatus getStatus() {
    return status;
  }


  /** Bind data port listeners to ports. This method ignores those who refuse to bind.
   * @return TRUE if one or more listeners were bound or if there are no listeners at all, FALSE if all listeners failed
   */

 //绑定数据端口监听对象，如果没有需要绑定的对象，默认为成功
 //否则只要有一个绑定成功，也认为是成功
  protected boolean bindDataPortListeners() {
    if (dataPortListenerSet.list().size() == 0) return true;
    return dataPortListenerSet.bind() > 0;
  }


  /** Unbind data port listeners. This method ignores those who refuse to unbind.
   * @return TRUE if all bound listeners unbound, FALSE if some listeners failed to be unbound
   */
 //只有所有的数据监听端口对象都绑定失败的情况下，才返回true
  protected boolean unbindDataPortListeners() {
    int bound = dataPortListenerSet.boundNumber();
    int unbound = dataPortListenerSet.unbind();
    return unbound == bound;
  }


  /** Bind control connector
   * @return TRUE if connector was bound, FALSE if connector failed
   */
 //绑定控制连接
  protected boolean bindControlConnector() {
    try {
      controlConnector.bind();
      return true;
    } catch (Throwable e) {
      log.fatal("Cannot bind control connector on port "+controlConnector.getPort(), e);
      return false;
    }
  }


  /** Unbind control connector
   * @return TRUE if connector was unbound, FALSE if connector failed to unbind
   */
//解除控制连接的绑定
  protected boolean unbindControlConnector() {
    try {
      if (controlConnector.isBound()) controlConnector.unbind();
      return true;
    } catch (Throwable e) {
      log.fatal("Cannot unbind control connector on port "+controlConnector.getPort(), e);
      return false;
    }
  }
}
