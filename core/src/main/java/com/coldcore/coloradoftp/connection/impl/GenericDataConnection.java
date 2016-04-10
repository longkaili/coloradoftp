package com.coldcore.coloradoftp.connection.impl;

import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.connection.*;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;
import com.coldcore.coloradoftp.session.Session;
import com.coldcore.coloradoftp.session.SessionAttributeName;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.Channel;

/**
 * @see com.coldcore.coloradoftp.connection.DataConnection
 */
/**一般的数据连接类，实现数据传输的基本功能
　所有的数据传输都是通过相应的Channel实现的
*/
public class GenericDataConnection extends GenericConnection implements DataConnection {

  private static Logger log = Logger.getLogger(GenericDataConnection.class);
  protected ControlConnection controlConnection; //数据连接对应的控制连接对象
  protected ReadableByteChannel rbc; //ReadableByteChannel是一个从Channel中读取数据，并保存到ByteBuffer的接口，使用read()方法读取
  protected WritableByteChannel wbc; //WritableByteChannel则是从ByteBuffer中读取数据，并输出到Channel的接口，使用write()方法写入
  protected DataConnectionMode mode; //数据连接的模式
  protected String filename; //操作的文件名
  protected boolean userAborted; //用户的“放弃”指令
  protected boolean successful; //数据传输是否成功
  protected boolean skipReply; //中断连接时，是否向用户发送相应的回复
  protected DataConnectionCallback callback;//数据连接对应的callback对象


  public GenericDataConnection(int bufferSize) {
    super();

    //rbuffer = ByteBuffer.allocateDirect(bufferSize);
    rbuffer = ByteBuffer.allocate(bufferSize); //初始化读任务的ByteBuffer
    rbuffer.flip();
  }


  /** Read data from user */
 /*从用户读取数据的函数*/
  protected void read() throws Exception {
    /* We will read data from the user and write it into the channel until the user
     * disconnects. There is no way to check if a complete file has been uploaded,
     * so we assume that every transfer is a success.
     */
     /**从用户读取数据并不断写入到Channel中去直到用户断开连接
        但是在这种情况下我们是不能知道数据传输到底是不是真正完成的
        所以我们默认每次从用户读取数据的过程都是成功的。
    */
    //Read data from user into the buffer if the buffer is empty
    if (!rbuffer.hasRemaining()) { //如果读数据的buffer为空，则SocketChannel向其中写入数据
      rbuffer.clear();
      int i = sc.read(rbuffer); //Thread blocks here...此处线程会阻塞
      rbuffer.flip();

      //Client disconnected?
      //客户端断开连接,抛出异常
      if (i == -1) {
        successful = true;
        throw new TransferCompleteException();
      }

      bytesRead += i;
      log.debug("Read from socket "+i+" bytes (total "+bytesRead+")");
    }

    //Forward the data into the channel
  //将从用户读取的数据写入到Channel中
    wbc.write(rbuffer);
  }


  /** Write data to user */
  /**向用户写数据*/
  protected void write() throws Exception {
    /* We wiil read data from the channel and write it to the user until the
     * channel is empty (successful transfer). If user disconnects earlier than
     * all data is transferred then the transfer has failed.
     */
   /**从Channel中读取数据然后写入到用户，直到Channel为空，此时可以认为数据写入成功
　　　如果用户在此过程中提前断开连接，则认为数据写入失败
　　　*/
    //Read the data from the channel into the buffer if the buffer is empty
    //如果rbuffer是空的，那么就利用rbc从Channel中读取数据，并写入到rbuffer中去
    if (!rbuffer.hasRemaining()) {
      rbuffer.clear();
      int i = rbc.read(rbuffer);
      rbuffer.flip();

      //File done?
      if (i == -1) {
        successful = true;
        throw new TransferCompleteException();
      }
    }

    //Forward the data to the user
    //将数据写入到用户中
    int i = sc.write(rbuffer); //Thread blocks here...此处，线程会阻塞

    //Client disconnected?
    if (i == -1) throw new TransferAbortedException();

    bytesWrote += i;
    log.debug("Wrote into socket "+i+" bytes (total "+bytesWrote+")");
  }


  /** Activate(激活) the connection if not active yet */
  /**如果当前连接未激活，则激活连接
　　   所谓的激活连接，就是从记录该数据连接用户信息的对象中
     获取信息(Mode,Channel等),并配置相应的类，启动读写线程的过程
　*/
  protected void activate() {
    /* The connection will start to function as soon as it gets MODE and CHANNEL from
     * user session (we must get CHANNEL last as it starts read/write routines).
     * Those attributes(属性) then have to be removed or the next data connection will use them as well.
     * There is also a FILENAME attribute for file operations.
     */

    if (rbc != null || wbc != null) return;//如果Channel块不为空，则意味着已经激活

    if (mode == null) { //获取对应的数据传输模式
      Session session = controlConnection.getSession();
      mode = (DataConnectionMode) session.getAttribute(SessionAttributeName.DATA_CONNECTION_MODE);
      if (mode != null) {
        log.debug("Mode extracted from user session");
      }
    }

    //Mode first
    if (mode == null) return; //如果模式为空，则返回

    if (filename == null) {//获得数据连接要操作的文件名
      Session session = controlConnection.getSession();
      filename = (String) session.getAttribute(SessionAttributeName.DATA_CONNECTION_FILENAME);
      if (filename != null) {
        log.debug("Filename extracted from user session");
      }
    }

    //Filename second
    if (mode != DataConnectionMode.LIST && filename == null) return;

    //Channel third (also start an appropriate thread)
    //最后获得对应的Channel块，并同时启动读写线程
    if (rbc == null && wbc == null) {
      Session session = controlConnection.getSession();
      if (mode == DataConnectionMode.LIST || mode == DataConnectionMode.RETR) {
        rbc = (ReadableByteChannel) session.getAttribute(SessionAttributeName.DATA_CONNECTION_CHANNEL);
        startWriterThread(); //To write data to user
      } else {
        wbc = (WritableByteChannel) session.getAttribute(SessionAttributeName.DATA_CONNECTION_CHANNEL);
        startReaderThread(); //To read data from user
      }
      if (rbc != null || wbc != null) {
        log.debug("Channel extracted from user session (data transfer begins)");
      }
    }
  }


  //连接服务程序，服务激活连接
  public void service() throws Exception {
    //User aborted the transfer
    if (userAborted) throw new TransferAbortedException();

    //Try to activate the data transfer
    activate();
  }


  /** Close data channel */
  /**关闭数据连接的channel*/
  protected void closeDataChannel() {
    Session session = controlConnection.getSession();
    Channel odc = (Channel) session.getAttribute(SessionAttributeName.DATA_CONNECTION_CHANNEL);
    session.removeAttribute(SessionAttributeName.DATA_CONNECTION_FILENAME);
    try {
      if (odc != null) odc.close();
    } catch (Throwable e) {
      log.error("Error closing data channel (ignoring)", e);
    }
  }


  /** Send reply to user upon connection termination */
  /**在连接终止的时候需要向用户发送对应的回复信息
　　   需要根据不同的终止情况(成功，用户主动终止命令，失败等)发送不同的回复
　　   这些回复由reply对象代表，由控制连接对应的数据连接发送
　　*/
  protected void reply() {
    try {
      //Transfer aborted by user - send "426" and then "226"
      if (userAborted) {
        Reply reply = (Reply) ObjectFactory.getObject(ObjectName.REPLY);
        reply.setCode("426");
        reply.setText("Connection closed, transfer aborted(失败).");
        controlConnection.reply(reply);

        reply = (Reply) ObjectFactory.getObject(ObjectName.REPLY);
        reply.setCode("226");
        reply.setText("Abort command successful.");
        controlConnection.reply(reply);

        log.debug("User aborted data transfer");
        return;
      }

      //Transfer failed
      if (!successful) {
        Reply reply = (Reply) ObjectFactory.getObject(ObjectName.REPLY);
        reply.setCode("426");
        reply.setText("Connection closed, transfer aborted.");
        controlConnection.reply(reply);

        log.debug("Data transfer failed");
        return;
      }

      //Transfer OK (note that STOU has a different code)
      Reply reply = (Reply) ObjectFactory.getObject(ObjectName.REPLY);
      if (mode == DataConnectionMode.STOU) reply.setCode("250");
      else reply.setCode("226");

      if (mode == DataConnectionMode.LIST) {
        reply.setText("Transfer completed.");
      } else {
        //Encode double-quated in the filename
        String encf = filename.replaceAll("\"", "\"\"");
        reply.setText("Transfer completed for \""+encf+"\".");
      }
      controlConnection.reply(reply);

      log.debug("Data transfer successful");

    } catch (Throwable e) {
      log.error("Error sending completion reply (ignoring)", e);
    }
  }



     //中断数据连接
   //1.关闭对应的Channel
    //2.清除对应的session中的属性
   //3.将其对应的控制连接中的数据连接引用清空
  public synchronized void destroy() {
    if (controlConnection != null) {
      closeDataChannel();

      //Hook for post-upload/download logic via a callback
      if (!skipReply && callback != null)
        try {
          if (successful) callback.onTransferComplete(this);
          else callback.onTransferAbort(this);
        } catch (Throwable e) {
          log.error("Callback error (ignoring)", e);
        }

      //When data transfer finishes, a reply must be send to a user
      if (!skipReply) reply();

      //Clear the attributes to prevent misuse(误用) by future instances
      Session session = controlConnection.getSession();
      session.removeAttribute(SessionAttributeName.DATA_CONNECTION_MODE);
      session.removeAttribute(SessionAttributeName.DATA_CONNECTION_CHANNEL);

      //Clear control connection reference
      controlConnection.setDataConnection(null);
    } else {
      log.warn("No control connection in the destroy method");
    }

    super.destroy();
  }


  //中断连接，但是不向用户发送相应的回复
  public void destroyNoReply() {
    skipReply = true;
    destroy();
  }


  //用户中断命令执行
  public void abort() {
    userAborted = true;
  }


  //获得该数据连接对应到控制连接对象
  public ControlConnection getControlConnection() {
    return controlConnection;
  }


  //设置该数据连接对应的控制连接对象
  public void setControlConnection(ControlConnection controlConnection) {
    this.controlConnection = controlConnection;
  }


  //获得该数据连接对应的CallBack对象
  public DataConnectionCallback getDataConnectionCallback() {
    return callback;
  }

  //设置该数据连接对应的CallBack对象
  public void setDataConnectionCallback(DataConnectionCallback callback) {
    this.callback = callback;
  }
}
