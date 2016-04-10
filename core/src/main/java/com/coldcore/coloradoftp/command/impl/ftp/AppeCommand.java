/**
 * Command APPE.
 * See FTP spec for details on the command.
 */
package com.coldcore.coloradoftp.command.impl.ftp;

import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.filter.DataFilter;
import com.coldcore.coloradoftp.filter.DataFilterFactory;
import com.coldcore.coloradoftp.session.Session;
import com.coldcore.coloradoftp.session.SessionAttributeName;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;

/**
  "追加(和新建)(APPE)"指令，该指令让服务器通过数据连接接受数据传输，并且将数据存储为
　服务器站点的一个文件，如果指定的路径名的文件在服务器站点已存在，那么将数据追加到该文件之后
  如果指定的文件不存在，那么在服务器站点新建一个文件．
　该指令也是在Stor指令的基础上实现的．
*/

public class AppeCommand extends StorCommand {


  public Reply execute() {
    Reply reply = getReply();
    if (!testLogin()) return reply;

    Session session = controlConnection.getSession();
    DataFilter filter = getTypeFilter(session);
    if (filter != null && filter.mayModifyDataLength()) {//不支持追加的数据类型
      reply.setCode("550");
      reply.setText("APPE unavailable for TYPE "+filter.getName()+".");
      return reply;
    }

    return super.execute();
  }


  protected boolean isAppend() {
    return true;
  }


  /** Load filter for TYPE
   * @param session User session
   * @return Data filter (never NULL)
   */
  protected DataFilter getTypeFilter(Session session) {
    String type = (String) session.getAttribute(SessionAttributeName.DATA_TYPE);
    if (type == null) type = "A";
    DataFilterFactory factory = (DataFilterFactory) ObjectFactory.getObject(ObjectName.TYPE_FILTER_FACTORY);
    return factory.create(type);
  }
}
