/**
 * Command STOU.
 * See FTP spec for details on the command.
 */
package com.coldcore.coloradoftp.command.impl.ftp;

import com.coldcore.coloradoftp.connection.DataConnectionMode;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;
import com.coldcore.coloradoftp.filesystem.FileSystem;
import com.coldcore.coloradoftp.filesystem.ListingFile;
import com.coldcore.coloradoftp.session.Session;
import org.apache.log4j.Logger;

/**
   "唯一保存(STOU)"指令，此命令和STOR相似，它要求被建立的文件在当前目录下的文件名是唯一的
   250回复必须包含产生的文件名
   直接在Stor命令的基础上产生
*/
public class StouCommand extends StorCommand {

  private static Logger log = Logger.getLogger(StouCommand.class);


  protected String getFilename() {
    Session session = controlConnection.getSession();
    FileSystem fileSystem = (FileSystem) ObjectFactory.getObject(ObjectName.FILESYSTEM);

    //Construct filename (must have the same name as the current folder user is in)
    String curDir = fileSystem.getCurrentDirectory(session);
    String parent = fileSystem.getParent(curDir, session);
    ListingFile lf = fileSystem.getPath(curDir, session);
    String filename = lf.getAbsolutePath().equals(parent) ? "" : lf.getName();

    log.debug("Constructed filename: "+filename);
    return filename;
  }


//返回数据连接模式为STOU
  protected DataConnectionMode getDataConnectionMode() {
    return DataConnectionMode.STOU;
  }
}
