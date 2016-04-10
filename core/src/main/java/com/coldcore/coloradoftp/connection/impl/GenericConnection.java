package com.coldcore.coloradoftp.connection.impl;

import com.coldcore.coloradoftp.connection.Connection;
import com.coldcore.coloradoftp.connection.TerminatedException;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @see com.coldcore.coloradoftp.connection.Connection
 *
 * Base class for all connections.
 */
/**Connection接口的一个抽象类实现
　  可以作为所有连接类的父类使用
*/
abstract public class GenericConnection implements Connection {

 /**需要子线程去执行的任务接口
　具体的任务分为读数据任务和写数据任务，由对应的连接类去实现
　*/
  /** Target to run */
  protected interface Target {
    /** Get target name */
/**取得任务的名字*/
    public String getName();
    /** Do target action and rest */
    /**执行具体的任务*/
    public void doTask() throws Exception;
  }


  /** Target runner */
  /**任务对应的线程*/
  protected class Runner implements Runnable {

    protected Target target;

    public Runner(Target target) {
      this.target = target;
    }
    public void run() {
      try {
          //只要连接没有断开，就执行任务
        while (!destroyed) {
          //Run the target
          target.doTask();
          Thread.yield();
        }
            //处理各种异常，并保证关闭连接
      } catch (TerminatedException e) {

        //Normal termination (exception has the message)
        log.debug(e.toString());
        try {
          destroy();
        } catch (Throwable ex) {}

      } catch (Throwable e) {

        //If something shut down the connection then this is no error
        if (!goingDown) {
          //Error termination
          log.error("Error in "+target.getName()+" thread", e);
          try {
            destroy();
          } catch (Throwable ex) {}
        }

      }
      log.debug(target.getName()+" thread finished");
    }
  }


  /** Data reader thread */
  //读数据类
  protected class Reader implements Target {
    public String getName() {
      return "Reader";
    }
    public void doTask() throws Exception {
      read();//核心是read()函数
    }
  }

  /** Data writer thread */
  //写数据类
  protected class Writer implements Target {
    public String getName() {
      return "Writer";
    }
    public void doTask() throws Exception {
      write();
    }
  }


  private static Logger log = Logger.getLogger(GenericConnection.class);
  protected SocketChannel sc; //该连接对应的SocketChannel
  protected boolean poisoned;//连接是否处于poisoned状态
  protected boolean destroyed;//连接是否已经断开
  protected long bytesWrote;//读取的比特数
  protected long bytesRead; //写入的比特数
  protected ByteBuffer rbuffer; //读任务的缓冲区
  protected ByteBuffer wbuffer;//写任务的缓冲区
  protected long sleep;//线程睡眠时间(单位毫秒)
  protected boolean goingDown; //是否为下载

 
  //默认线程睡眠时间100毫秒
  public GenericConnection() {
    sleep = 100L;
  }


  //返回该连接对应的SocketChannel
  public SocketChannel getSocketChannel() {
    return sc;
  }

  //初始化，其实就是指定该连接对应的SocketChannel，使用synchronized防止多线程的并发初始化
  public synchronized void initialize(SocketChannel channel) {
    if (sc != null) {//如初始化后再次初始化则抛出异常
      log.warn("Attempt to re-initialize an initialized connection");
      throw new IllegalStateException("Already initialized");
    }

    sc = channel;
  }


  /** Start reader thread */
  /**启动读数据线程*/
  protected void startReaderThread() {
    new Thread(new Runner(new Reader())).start();
  }


  /** Start writer thread */
 /**启动写数据线程*/
  protected void startWriterThread() {
    new Thread(new Runner(new Writer())).start();
  }


//修改destroyed标志，使得子线程正确关闭
 //使用synchronize防止并发修改；修改标志的同时需要关闭底层IO资源
  public synchronized void destroy() {
    goingDown = true;
    if (destroyed) return;

    //Close the channel
    try {
      sc.close();
    } catch (Throwable e) {
      log.error("Cannot close the channel (ignoring)", e);
    }

    destroyed = true;
    log.debug("Connection destroyed");
  }


  //将连接poisoned变成true
  public void poison() {
    poisoned = true;
    log.debug("Connection poisoned");
  }

//判断是否断开连接
  public boolean isDestroyed() {
    return destroyed;
  }

  //判断连接是否处于poisoned状态
  public boolean isPoisoned() {
    return poisoned;
  }


 //获得写入的字节数
  public long getBytesWrote() {
    return bytesWrote;
  }


//获得读取的字节数
  public long getBytesRead() {
    return bytesRead;
  }


  //下面的三个抽象方法由具体的连接实现
  /** Read data from user routine */
  /**从用户程序读取数据*/
  abstract protected void read() throws Exception;


  /** Write data to user routine */
  /**向用户程序写数据*/
  abstract protected void write() throws Exception;


  abstract public void service() throws Exception;


  /** Get thread sleep time
   * @return Time in mills
   */
  public long getSleep() {
    return sleep;
  }


  /** Set thread sleep time
   * @param sleep Time in mills
   */
  public void setSleep(long sleep) {
    this.sleep = sleep;
  }
}
