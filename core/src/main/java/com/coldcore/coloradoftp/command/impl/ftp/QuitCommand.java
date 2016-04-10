/**
 * Command QUIT.
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
import com.coldcore.coloradoftp.session.Session;
import com.coldcore.coloradoftp.session.SessionAttributeName;


/**
  "退出登录(QUIT)"指令，不需要任何参数
   该指令终止一个用户，如果没有正在执行的文件传输，服务器将关闭控制连接
   如果有数据连接，在得到传输响应后，服务器关闭控制连接．
*/
public class QuitCommand extends AbstractCommand {

  public Reply execute() {
    Reply reply = getReply();
    if (!testLogin()) return reply;//未登录

    //Abort data connection initiator
  //终止数据连接生成器
    controlConnection.getDataConnectionInitiator().abort();

    //Abort data connection listeners
   //终止该控制连接的数据端口侦听
    DataPortListenerSet listeners = (DataPortListenerSet) ObjectFactory.getObject(ObjectName.DATA_PORT_LISTENER_SET);
    for (DataPortListener listener : listeners.list())
      listener.removeConnection(controlConnection);

    //Logout the user
   //用户登出
    logout();

    /* We must not clear login state of the user, instead we will poison her connection
     * to make sure she cannot send any more commands in (except for special commands).
     * And set the byte marker on this poisoned connection so it will wait for a reply to be
     * added to its outgoing buffer before attempting to kill itself in the "service" method.
     */
     /**该用户的登录信息并没有被清除，我们只是将其控制连接变成"poisoend"状态*/
    Session session = getConnection().getSession();
    //记录服务器收到的用户字节数
    session.setAttribute(SessionAttributeName.BYTE_MARKER_POISONED, controlConnection.getBytesWrote());
    controlConnection.poison();//将控制连接置为"poisoned"状态

    DataConnection dataConnection = controlConnection.getDataConnection();
    if (dataConnection != null) {
      reply.setCode("221");
      reply.setText("Logged out, closing control connection as soon as data transferred.");
    } else {
      reply.setCode("221");
      reply.setText("Logged out, closing control connection.");
    }

    return reply;
  }


  public boolean processInInterruptState() {
    return true;
  }


  /** Log out user.
   * This implementation does nothing.
   */
  protected void logout() {
  }
}
