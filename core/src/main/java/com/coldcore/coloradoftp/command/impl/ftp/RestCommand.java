/**
 * Command REST.
 * See FTP spec for details on the command.
 */
package com.coldcore.coloradoftp.command.impl.ftp;

import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.command.impl.AbstractCommand;
import com.coldcore.coloradoftp.session.Session;
import com.coldcore.coloradoftp.session.SessionAttributeName;
import com.coldcore.coloradoftp.filter.DataFilter;
import com.coldcore.coloradoftp.filter.DataFilterFactory;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;

/*
  "重新开始(REST)"指令，该指令的参数代表服务器要重新开始的文件传输的标记
  该命令并不传输文件，而是跳到文件的指定数据检查点．此命令后应该紧跟合适的
  使数据重传的FTP指令．
**/
public class RestCommand extends AbstractCommand {
             
  public Reply execute() {
    Reply reply = getReply();
    if (!testLogin()) return reply;

    String marker = getParameter();//取得标记
    if (marker.equals("")) {//标记为空，则返回
      reply.setCode("501");
      reply.setText("Send correct marker.");
      return reply;
    }

    Session session = getConnection().getSession();
    session.removeAttribute(SessionAttributeName.DATA_MARKER);

    DataFilter filter = getTypeFilter(session);
    if (filter != null && filter.mayModifyDataLength() && !marker.equals("0")) {
      reply.setCode("550");
      reply.setText("REST unavailable for TYPE "+filter.getName()+".");
      return reply;
    }

    try {
      long l = Long.parseLong(marker);
      if (l < 0) throw new Exception();
      //设定文件检查点，下次从该位置重新存储数据
      session.setAttribute(SessionAttributeName.DATA_MARKER, l);
    } catch (Throwable e) {
      reply.setCode("501");
      reply.setText("Send correct marker.");
      return reply;
    }

    reply.setCode("350");
    reply.setText("Marker set.");
    return reply;
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
