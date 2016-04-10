package com.coldcore.coloradoftp.connection.impl;

import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.connection.ConnectionPool;
import com.coldcore.coloradoftp.connection.ControlConnection;
import com.coldcore.coloradoftp.connection.DataConnection;
import com.coldcore.coloradoftp.connection.DataConnectionInitiator;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;
import com.coldcore.coloradoftp.session.Session;
import com.coldcore.coloradoftp.session.SessionAttributeName;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @see com.coldcore.coloradoftp.connection.DataConnectionInitiator
 */
/**用户向控制连接发送IP和端口号，控制连接利用这个对象主动新建一个数据连接，向用户发送数据*/
public class GenericDataConnectionInitiator implements DataConnectionInitiator, Runnable {

  private static Logger log = Logger.getLogger(GenericDataConnectionInitiator.class);
  protected String ip;//用户IP
  protected int port; //用户端口
  protected boolean active;//active状态
  protected  SocketChannel sc;
  protected ControlConnection controlConnection;//对应的控制连接
  protected ConnectionPool dataConnectionPool;//对应的数据连接池
  protected Reply errorReply;//对应的错误回复对象
  protected Thread thr;//服务线程
  protected long sleep;//线程睡眠时间
  protected boolean aborted;//用户是否执行"ABOR"命令


  //默认睡眠时间100毫秒
  public GenericDataConnectionInitiator() {
    sleep = 100L;
  }


  //获得错误信息的reply对象
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
  //获取线程的睡眠时间
  
  public long getSleep() {
    return sleep;
  }


  /** Set thread sleep time
   * @param sleep Time in mills
   */
  //设定线程的睡眠时间(以毫秒为单位)
  public void setSleep(long sleep) {
    this.sleep = sleep;
  }


  /** Test if user got a "150" reply
   * @return TRUE if user got the reply, FALSE if not yet
   */
  /**测试用户是否获得150回复
　　　　　@return 获得150回复则回复true,否则回复false
　　*/
  protected boolean isReply150() {
    Session session = controlConnection.getSession();
    Long bytesWrote = (Long) session.getAttribute(SessionAttributeName.BYTE_MARKER_150_REPLY);
    if (bytesWrote == null || controlConnection.getBytesWrote() == bytesWrote || controlConnection.getOutgoingBufferSize() != 0) return false;
    log.debug("User got a 150 reply");
    return true;
  }


  //获得用户IP
  public String getIp() {
    return ip;
  }


  //设定用户IP
  public void setIp(String ip) {
    this.ip = ip;
  }


 //获得用户端口
  public int getPort() {
    return port;
  }


 //设定用户端口
  public void setPort(int port) {
    this.port = port;
  }


 //数据连接初始化对象的服务线程
  public void run() {
    while (active) { //处于激活状态就可以执行下面的程序

      DataConnection dataConnection = null;
      try {

        /* We cannot open the socket yet. We must wait until user receives the positive "150" reply.
         * The reply might not be in the buffer of the control connection just yet.
         */
        //规定只有在用户获得150回复之后，才能建立数据连接
        if (!isReply150()) {
          Thread.sleep(sleep);
          continue;
        }

        //Get required objects
       //获得数据连接池
        dataConnectionPool = (ConnectionPool) ObjectFactory.getObject(ObjectName.DATA_CONNECTION_POOL);

        //Configure socket and connect
      //配置socket并进行连接
         sc = SocketChannel.open();
        sc.connect(new InetSocketAddress(ip, port)); //Thread blocks here...
        if (!sc.finishConnect()) throw new RuntimeException("Failed finishConnect");
        String ip = sc.socket().getInetAddress().getHostAddress();
        log.debug("New data connection established (IP "+ip+")");

        //Create new connection instance
        //创建一个新的数据连接
        dataConnection = (DataConnection) ObjectFactory.getObject(ObjectName.DATA_CONNECTION);
        dataConnection.initialize(sc);

        //If there is a data connection already then kill it
        //如果对应的控制连接已经存在一个数据连接，中断那个连接
        DataConnection existing = controlConnection.getDataConnection();
        if (existing != null && !existing.isDestroyed()) {
          log.warn("BUG: Replacing existing data connection with a new one!");
          existing.destroyNoReply();
        }

        //Configure the data connection and wire it with the control connection and add to pool
     //配置数据连接并将其加入到数据连接池中
        controlConnection.setDataConnection(dataConnection);
        dataConnection.setControlConnection(controlConnection);
        configure(dataConnection);
        dataConnectionPool.add(dataConnection);
        log.debug("New data connection is ready");

        active = false;//设定active状态为false，表示连接已经建好

      } catch (Throwable e) {

        //If aborted then do not post an error message
        if (!aborted) {
          log.warn("Failed to establish a connection with "+ip+":"+port+" (ignoring)", e);
          try {
            dataConnection.destroyNoReply();
          } catch (Throwable ex) {}
          try {
            sc.close();
          } catch (Throwable ex) {
            log.error("Cannot close the channel (ignoring)", e);
          }

          controlConnection.reply(getErrorReply());
        }

        active = false;
      }

    }
    log.debug("Data connection initiator thread finished");
  }

   
  //是否处于运行状态
  public boolean isActive() {
    return active;
  }


 //激活初始化程序
  public synchronized void activate() {
    if (active) {
      log.warn("Data connection initiator was active when activate routine was called");
      return;
    }

    active = true;
    aborted = false;

    //Start this class
   //开启一个线程对数据连接进行配置
    thr = new Thread(this);
    thr.start();
  }


  //用户执行"放弃指令"
  public synchronized void abort() {
    aborted = true;
    if (!active) return;

    //Close the channel
    try {
      if (sc != null && sc.isOpen()) sc.close();
    } catch (Throwable e) {
      log.error("Cannot close channel (ignoring)", e);
    }

    controlConnection.reply(getErrorReply());

    //Clear the attribute to prevent misuse by future instances
    Session session = controlConnection.getSession();
    session.removeAttribute(SessionAttributeName.BYTE_MARKER_150_REPLY);

    active = false;
  }


  public ControlConnection getControlConnection() {
    return controlConnection;
  }


  public void setControlConnection(ControlConnection controlConnection) {
    this.controlConnection = controlConnection;
  }


  /** Configure connection before adding it to a pool
   * @param connection Connection
   */
   //在将数据连接添加到数据连接池中前对其进行配置
  public void configure(DataConnection connection) {
  }
}
