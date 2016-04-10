/**
 * Command STOR.
 * See FTP spec for details on the command.
 *
 * This class is designed as the superclass for STOU and APPE commands.
 */
package com.coldcore.coloradoftp.command.impl.ftp;

import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.command.impl.AbstractCommand;
import com.coldcore.coloradoftp.connection.DataConnectionMode;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;
import com.coldcore.coloradoftp.filesystem.FileSystem;
import com.coldcore.coloradoftp.filter.DataFilterApplicator;
import com.coldcore.coloradoftp.session.Session;
import com.coldcore.coloradoftp.session.SessionAttributeName;
import org.apache.log4j.Logger;

import java.nio.channels.WritableByteChannel;
import java.nio.channels.Channel;

/**
   "保存(STOR)"指令，该指令让服务器通过数据连接接受数据传输，并且把数据存储为该服务器站点的一个文件．
　　如果指定的路径名的文件在服务器站点已经存在，那么它的内容将被传输的数据替换．如果指定的路径名的文件
　　不存在，那么在服务器站点新建一个文件．
*/

public class StorCommand extends AbstractCommand {

  private static Logger log = Logger.getLogger(StorCommand.class);


  public Reply execute() {
    Reply reply = getReply();
    if (!testLogin()) return reply;//未登录

    String filename = getFilename();//从命令参数取得文件名
    if (filename.length() == 0) {//文件名为空，提示用户发送文件名
      reply.setCode("501");
      reply.setText("Send file name.");
      return reply;
    }

    //Test if there is data channel left in the session
   //如果前面还有遗留的数据连接，则先关闭
    closeSessionDataChannel();

    Session session = controlConnection.getSession();
 
    //调用文件系统的savaFile获得对应的wbc，然后用它建立数据连接，将数据写入到文件中
    FileSystem fileSystem = (FileSystem) ObjectFactory.getObject(ObjectName.FILESYSTEM);
    WritableByteChannel wbc = fileSystem.saveFile(filename, isAppend(), session);

    DataFilterApplicator applicator = (DataFilterApplicator) ObjectFactory.getObject(ObjectName.DATA_FILTER_APPLICATOR);
    wbc = applicator.applyFilters(wbc, session);

    String type = (String) session.getAttribute(SessionAttributeName.DATA_TYPE);
    if (type == null) type = "A";

    if (!prepareForDataConnection()) {
      try {
        wbc.close();
      } catch (Throwable e) {
        log.error("Error closing data channel (ignoring)", e);
      }
      return reply;
    }

    session.setAttribute(SessionAttributeName.DATA_CONNECTION_FILENAME, filename);
    session.setAttribute(SessionAttributeName.DATA_CONNECTION_MODE, getDataConnectionMode());
    session.setAttribute(SessionAttributeName.DATA_CONNECTION_CHANNEL, wbc);

    reply.setCode("150");
    reply.setText("Opening "+type+" mode data connection for "+filename+".");
    return reply;
  }


  /** Close a data channel if exists in the session */
  protected void closeSessionDataChannel() {
    Session session = controlConnection.getSession();
    Channel odc = (Channel) session.getAttribute(SessionAttributeName.DATA_CONNECTION_CHANNEL);
    if (odc != null) {
      log.debug("Attempting to close data channel in session");
      session.removeAttribute(SessionAttributeName.DATA_CONNECTION_CHANNEL);
      try {
        odc.close();
      } catch (Throwable e) {
        log.error("Error closing data channel (ignoring)", e);
      }
    }
  }
  

  /** Get filename to save
   * @return Filename
   */
  protected String getFilename() {
    return getParameter();
  }


  /** Get data connection mode to use
   * @return Data connection mode
   */
//返回数据连接的模式
  protected DataConnectionMode getDataConnectionMode() {
    return DataConnectionMode.STOR;
  }


  /** Test if file must be appended or overwritten
   * @return TRUE to append or FALSE to overwrite
   */
  //是否将内容追加到文件末尾，总是返回false
  protected boolean isAppend() {
    return false;
  }
}
