package com.coldcore.coloradoftp.connection.impl;

import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.connection.ConnectionPool;
import com.coldcore.coloradoftp.connection.ControlConnection;
import com.coldcore.coloradoftp.connection.DataConnection;
import com.coldcore.coloradoftp.connection.DataPortListener;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * @see com.coldcore.coloradoftp.connection.DataPortListener
 */
/**数据连接端口监听对象，在指定端口监听*/
public class GenericDataPortListener implements DataPortListener, Runnable {

  private static Logger log = Logger.getLogger(GenericDataPortListener.class);
  protected int port;//监听的端口
  protected String host;//本地主机IP
  protected boolean bound;//是否已绑定
  protected ServerSocketChannel ssc;//本端口对应的ServerSocketChannel
  protected Map<String, ControlConnection> awaiting;//等待连接的控制连接
  protected ConnectionPool dataConnectionPool;//数据连接池
  protected Reply errorReply;//错误答复对象
  protected Thread thr;//服务线程
  protected long sleep;//线程睡眠时间


//进行一些初始化
  public GenericDataPortListener() {
    port = -1;
    sleep = 100L;
    awaiting = new HashMap<String,ControlConnection>();
  }


 //错误回复对象
  protected Reply getErrorReply() {
    if (errorReply == null) {
      errorReply = (Reply) ObjectFactory.getObject(ObjectName.REPLY);
      errorReply.setCode("425");
      errorReply.setText("Can't open data connection.");
    }
    return errorReply;
  }


  /** Get thread sleep time
   * @return Time in mills
   */
  //取得线程睡眠时间
  public long getSleep() {
    return sleep;
  }


  /** Set thread sleep time
   * @param sleep Time in mills
   */
//设置线程睡眠时间
  public void setSleep(long sleep) {
    this.sleep = sleep;
  }

 
  //获取绑定的端口号
  public int getPort() {
    return port;
  }

 //设定需要绑定的端口号
  public void setPort(int port) {
    this.port = port;
  }

 
  //获取本地主机IP
  public String getHost() {
    return host;
  }


 //设置本地主机IP
  public void setHost(String host) {
    this.host = host;
  }


  //端口绑定
  public synchronized void bind() throws IOException {
    if (port < 1) throw new IllegalArgumentException("Set correct port first");//非法端口号

    if (bound) {//端口已经绑定
      log.warn("Listener on port "+port+" was bound when bind routine was submitted");
      throw new IllegalStateException("Unbind the listener on port "+port+" first");
    }

    //Get required objects
    //取得数据连接池
    dataConnectionPool = (ConnectionPool) ObjectFactory.getObject(ObjectName.DATA_CONNECTION_POOL);

    //Bind to the port
    //所谓绑定到端口，就是开启对应的ServerSocketChannel对象，然后将其与指定的端口关联
    InetSocketAddress isa = host == null ? new InetSocketAddress(port) : new InetSocketAddress(host, port);
    ssc = ServerSocketChannel.open();
    ssc.socket().bind(isa);

    //Start this class
//端口绑定以后，就要开启一个服务线程
    thr = new Thread(this);
    thr.start();

    bound = true;
    log.debug("Listener is bound to port "+port);
  }


//解除绑定，分两步
 //1.将该端口上等待的所有控制连接移除掉
 //2.关闭该端口上绑定的ServerSocketChannel
  public synchronized void unbind() throws IOException {
    if (!bound) {//如果还没绑定，当然不能解除，抛出异常
      log.warn("Listener on port "+port+" was not bound when unbind routine was submitted");
      throw new IllegalStateException("Cannot unbind the listener on port "+port+", it is not bound");
    }

    //Set this now to prevent run() method from flooding errors after the socket is closed
    bound = false;

    //Remove all awaiting connections
    //移除掉所有的控制连接
    synchronized (awaiting) {
      for (ControlConnection connection : awaiting.values())
        if (!connection.isDestroyed()) removeConnection(connection);
    }

    //Unbind from the port
    if (ssc.isOpen()) ssc.close();

    log.debug("Listener on port "+port+" is unbound");
  }


//返回端口绑定状态
  public boolean isBound() {
    return bound;
  }


 //向等待的控制连接Map中添加新的控制连接
  public boolean addConnection(ControlConnection connection) {
    if (!bound) return false;

    //清理掉Map中没有必要的控制连接
    //Clean up the map from unnecessary control connections
    cleanup();

    //Add a new one
   //添加一个新的控制连接,key为该控制连接对应的客户端IP地址，value为控制连接对象本身
    String ip = connection.getSocketChannel().socket().getInetAddress().getHostAddress();
    synchronized (awaiting) {
      ControlConnection con = awaiting.get(ip);
      if (con != null && con != connection) return false;//如果已经该控制连接已经存在，则返回false
      awaiting.put(ip, connection);
      return true;
    }
  }


//从等待数据连接的Map中移除一个控制连接，通过IP地址移除
  public boolean removeConnection(ControlConnection connection) {
    String ip = connection.getSocketChannel().socket().getInetAddress().getHostAddress();
    synchronized (awaiting) {
      ControlConnection c = awaiting.get(ip);
      if (c == connection) {
        awaiting.remove(ip);
        c.reply(getErrorReply());
        return true;
      }
      return false;
    }
  }


 //数据端口监听对象的任务运行体
//主要就是在这个端口利用ServerSocketChannel进行监听
 //如果有连接进入，就获取对应的SocketChannel，然后再
 //配置相应的数据连接，并将其加入到数据连接池中。
 //但要注意对于每一接入到数据连接，必须要有指定的控制连接与其对应才认为是有效的(通过IP地址进行识别)
  public void run() {
    while (bound) {//如果已经绑定了数据端口，则进行监听  
      ControlConnection controlConnection = null;
      DataConnection dataConnection = null;
      SocketChannel sc = null;
      try {
        sc = ssc.accept(); //Thread blocks here...
        String ip = sc.socket().getInetAddress().getHostAddress();
        log.debug("New incoming data connection (from "+ip+" on port "+port+")");

        //Create new connection instance
        dataConnection = (DataConnection) ObjectFactory.getObject(ObjectName.DATA_CONNECTION);
        dataConnection.initialize(sc);

        //Locate a control connection waiting for this data connection
      //取得对应的控制连接
        controlConnection = popControlConnection(dataConnection);
        if (controlConnection == null) {
          log.warn("No control connection found for an incoming data connection (from "+ip+" on port "+port+")");
          dataConnection.destroyNoReply();
        } else {

          //If there is a data connection already then kill it
         //如果已该控制连接已经有了数据连接，则关闭那个数据连接
          DataConnection existing = controlConnection.getDataConnection();
          if (existing != null && !existing.isDestroyed()) {
            log.warn("BUG: Replacing existing data connection with a new one!");
            existing.destroyNoReply();
          }

          //Configure the data connection and wire it with the control connection and add to the pool
          controlConnection.setDataConnection(dataConnection);
          dataConnection.setControlConnection(controlConnection);
          configure(dataConnection);
          dataConnectionPool.add(dataConnection);
          log.debug("New data connection is ready");
        }

        Thread.sleep(sleep);//周期性监听

      } catch (Throwable e) {
        if (bound) log.warn("Failed to accept a connection (ignoring)", e);
        try {
          dataConnection.destroyNoReply();
        } catch (Throwable ex) {}
        try {
          sc.close();
        } catch (Throwable ex) {
          if (bound) log.error("Cannot close the channel (ignoring)", e);
        }

        //Send error reply
        if (controlConnection != null) controlConnection.reply(getErrorReply());
      }

    }
    log.debug("Data port listener thread finished");
  }


  /** Cleas up the map from connections which should not be in it */
//移除掉那些已经断开的控制连接
  protected void cleanup() {
    synchronized (awaiting) {
      for (String ip : awaiting.keySet()) {
        ControlConnection connection = awaiting.get(ip);
        if (connection.isDestroyed()) awaiting.remove(ip);
      }
    }
  }


  /** Locate a control connection which awaits for a data connection and remove it
   * @param dataConnection Incoming data connection
   * @return Control connection or NULL if a control connection cannot be located and the data connection should be dropped
   */
  //通过ip地址找到数据连接对应的控制连接，然后将其从等待的Map中移除，注意如果该控制连接已经中断了，要返回null
  protected ControlConnection popControlConnection(DataConnection dataConnection) {
    String dip = dataConnection.getSocketChannel().socket().getInetAddress().getHostAddress();
    synchronized (awaiting) {
      for (String ip : awaiting.keySet()) {
        if (ip.equals(dip)) {
          ControlConnection controlConnection = awaiting.remove(ip);
          return controlConnection.isDestroyed() ? null : controlConnection;
        }
      }
    }
    return null;
  }


  /** Configure connection before adding it to a pool
   * @param connection Connection
   */
  //对数据连接进行配置
  public void configure(DataConnection connection) {
  }
}
