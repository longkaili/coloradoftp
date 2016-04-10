/**
 * Command ABOR.
 * See FTP spec for details on the command.
 */
package com.coldcore.coloradoftp.command.impl.ftp;

import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.command.impl.AbstractCommand;
import com.coldcore.coloradoftp.connection.DataConnection;
import com.coldcore.coloradoftp.connection.DataPortListener;
import com.coldcore.coloradoftp.connection.DataPortListenerSet;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;
import org.apache.log4j.Logger;

/**
  "放弃(ABOR)"指令，该指令告诉服务器放弃此前的FTP指令，和任何相关的数据传输．
　　遇到该指令时，服务器不会关闭控制连接，但是会关闭数据连接．具体有两种情况：
　　　1)：FTP服务指令已经完成．服务器关闭数据连接(如果已经建立),然后返回226回应，指出这个放弃指令已经成功执行
   2)：FTP服务指令仍在执行．服务器放弃正在执行的FTP服务，关闭数据连接，返回426回应，指出服务器请求被异常终止
　　　然后服务器发送226回应，指出这个放弃指令已经成功执行．
*/

public class AborCommand extends AbstractCommand {

  private static Logger log = Logger.getLogger(AborCommand.class);


  public Reply execute() {
    Reply reply = getReply();
    if (!testLogin()) return reply;


//先终止数据连接创建对象，和指定串口上的侦听，以防止新的数据连接的生成
    //Abort data connection initiator
    controlConnection.getDataConnectionInitiator().abort();

    //Abort data connection listeners
    DataPortListenerSet listeners = (DataPortListenerSet) ObjectFactory.getObject(ObjectName.DATA_PORT_LISTENER_SET);
    for (DataPortListener listener : listeners.list())
      listener.removeConnection(controlConnection);

    //Abort active data connection
      // 然后如果数据连接不为空，则终止数据连接
    DataConnection dataConnection = controlConnection.getDataConnection();
    if (dataConnection != null) {
      log.debug("Aborting data connection, it will send reply to ABOR command");
      dataConnection.abort();
      return null;
    } else {
      reply.setCode("226");
      reply.setText("Abort command successful.");
      return reply;
    }
  }

  
  //这个指令可以在中断状态执行的
  public boolean processInInterruptState() {
    return true;
  }

  //可以清除服务器的中断状态
  public boolean canClearInterruptState() {
    return true;
  }

}
