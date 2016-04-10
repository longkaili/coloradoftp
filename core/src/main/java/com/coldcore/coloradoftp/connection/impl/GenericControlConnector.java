package com.coldcore.coloradoftp.connection.impl;

import com.coldcore.coloradoftp.command.Command;
import com.coldcore.coloradoftp.command.CommandProcessor;
import com.coldcore.coloradoftp.connection.ConnectionPool;
import com.coldcore.coloradoftp.connection.ControlConnection;
import com.coldcore.coloradoftp.connection.ControlConnector;
import com.coldcore.coloradoftp.core.Core;
import com.coldcore.coloradoftp.core.CoreStatus;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @see com.coldcore.coloradoftp.connection.ControlConnector
 */
/**控制连接生成类，在指定的控制端口进行监听，如果有连接接入，就生成一个控制连接
　　FTP服务器启动时会先启动这个类*/

public class GenericControlConnector implements ControlConnector, Runnable {

  private static Logger log = Logger.getLogger(GenericControlConnector.class);
  protected ServerSocketChannel ssc;//服务器在控制端口监听的ServerSocketChannel
  protected int port;//监听的端口
  protected String host; //本机网络地址
  protected boolean bound; //是否已绑定
  protected Core core; //FTP服务器对象,主要是有时候要判断当前服务器的状态
  protected CommandProcessor commandProcessor;//执行用户命令并返回结果的对象
  protected ConnectionPool controlConnectionPool; //控制连接池
  protected Thread thr; //线程对象,服务器启动时会启动一个线程执行控制端口的监听(这个类就是线程的任务执行体)
  protected long sleep; //睡眠时间(监听不是不间断的而是周期性的，所以线程需要睡眠一段时间)


  public GenericControlConnector() {
    port = 9800; //默认侦听端口,原定的21端口已经被占用.
    sleep = 100L; //默认睡眠时间
  }


  /** Configure connection before adding it to a pool
   * @param connection Connection
  */

/**在将控制连接放入连接池前，对其进行配置
  ＠param  控制连接对象*/
  public void configure(ControlConnection connection) {
    if (core.getStatus() == CoreStatus.POISONED) {//如果服务器处于poisoned状态,则向用户发送poisoned消息,并中断连接
      //Server is shutting down, reply and poison the connection
      Command command = (Command) ObjectFactory.getObject(ObjectName.COMMAND_POISONED);//command类代表用户的命令
      command.setConnection(connection);
      commandProcessor.execute(command);
      connection.poison();
    } else {//服务器处于运行状态，则正常执行命令
      //Server is ready, reply so
      Command command = (Command) ObjectFactory.getObject(ObjectName.COMMAND_WELCOME);//向用户发送欢迎消息
      command.setConnection(connection);
      commandProcessor.execute(command);
    }
  }


  //端口绑定,绑定的同时就会开启服务线程来进行监听
  public synchronized void bind() throws IOException {
    if (bound) {//如果已经绑定了，则抛出异常
      log.warn("Connector on port "+port+" was bound when bind routine was submitted");
      throw new IllegalStateException("Unbind the connector on port "+port+" first");
    }

    //Get required objects
    //获取绑定所需的对象,使用工厂方法获得，有些对象是全局唯一的，而有些对象是每次都会分配的
    core = (Core) ObjectFactory.getObject(ObjectName.CORE);
    commandProcessor = (CommandProcessor) ObjectFactory.getObject(ObjectName.COMMAND_PROCESSOR);
    controlConnectionPool = (ConnectionPool) ObjectFactory.getObject(ObjectName.CONTROL_CONNECTION_POOL);

    //Bind to the host and port
 //绑定ip和端口
    //所谓的绑定其实就是在指定主机的指定端口开启一个ServerSocketChannel进行监听
    InetSocketAddress isa = host == null ? new InetSocketAddress(port) : new InetSocketAddress(host, port);
    ssc = ServerSocketChannel.open();
    ssc.socket().bind(isa);

    //Start this class
//开启一个服务线程
    thr = new Thread(this);
    thr.start();

    bound = true;
    log.info("Connector is bound to port "+port);
  }


 //解除绑定，即关闭对应的ServerSocektChannel
  public synchronized void unbind() throws IOException {
    if (!bound) {
      log.warn("Connector on port "+port+" was not bound when unbind routine was submitted");
      throw new IllegalStateException("Cannot unbind the connector on port "+port+", it is not bound");
    }

    //Set this now to prevent run() method from flooding errors after the socket is closed
    bound = false;//修改绑定状态

    //Unbind from the port
    if (ssc.isOpen()) ssc.close();

    //Wait for this class to stop (just in case)
//等待当前线程完成
    try {
      thr.join(30000);
    } catch (Throwable e) {}

    log.info("Connector on port "+port+" is unbound");
  }


 //返回是否已经绑定
  public boolean isBound() {
    return bound;
  }


 //线程执行体，周期性的在绑定的端口监听，当有连接请求时就新建一个控制连接
  public void run() {
    while (bound) { 
      ControlConnection connection = null;
      SocketChannel sc = null;//控制连接对应的SocketChanel
      try {
        sc = ssc.accept(); //Thread blocks here...线程会在这里阻塞
        String ip = sc.socket().getInetAddress().getHostAddress();
        log.debug("New control connection accepted (IP "+ip+")");

        //Create new connection instance and initialize it
        //创建新的控制连接并利用当前获得的SocketChannel进行初始化
        connection = (ControlConnection) ObjectFactory.getObject(ObjectName.CONTROL_CONNECTION);
        connection.initialize(sc);

        //Configure the control connection and add to pool
        //配置控制连接并将其加入连接池，配置控制连接是指根据当前服务器的状态，向用户发送不同的信息，以表明连接是否成功建立
        configure(connection);
        controlConnectionPool.add(connection);
        log.debug("New control connection is ready");
        Thread.sleep(sleep);

      } catch (Throwable e) {
        if (bound) log.warn("Failed to accept a connection (ignoring)", e);
        try {
          connection.destroy();
        } catch (Throwable ex) {}
        try {
          sc.close();
        } catch (Throwable ex) {}
      }

    }
    log.debug("Control connector thread finished");
  }


 //获得绑定的端口
  public int getPort() {
    return port;
  }


//设置绑定端口
  public void setPort(int port) {
    if (port < 1) throw new IllegalArgumentException("Invalid port");
    this.port = port;
  }


//获得主机IP地址
  public String getHost() {
    return host;
  }

  //设置主机IP地址
  public void setHost(String host) {
    this.host = host;
  }


  /** Get thread sleep time
   * @return Time in mills
   */
  //获得线程睡眠时间，即周期性监听时间
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
}
