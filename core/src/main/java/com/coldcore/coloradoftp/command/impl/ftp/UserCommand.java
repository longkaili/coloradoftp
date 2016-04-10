/**
 * Command USER.
 * See FTP spec for details on the command.
 */
package com.coldcore.coloradoftp.command.impl.ftp;

import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.command.impl.AbstractCommand;
import com.coldcore.coloradoftp.session.LoginState;
import com.coldcore.coloradoftp.session.Session;
import com.coldcore.coloradoftp.session.SessionAttributeName;
import org.apache.log4j.Logger;


/**
  "用户名(USER)"命令，它的参数是用来指定用户的Telnet字符串．它用来进行用户鉴定，服务器对赋予文件的系统访问权限.
  服务器运行用户为了改变访问控制或账户信息而重新发送"USER"命令，这会导致原有的账户信息被清空，且旧的
　传输进程会在原来的参数控制下完成．
　服务器允许进行匿名登录(其实就是不需要登录)，且默认的就是匿名登录
*/
public class UserCommand extends AbstractCommand {

  private static Logger log = Logger.getLogger(UserCommand.class);
  private boolean anonymous;


 //默认的就是匿名登录
  public UserCommand() {
    anonymous = true;
  }


  //是否为匿名登录
  public boolean isAnonymous() {
    return anonymous;
  }


//设置匿名登录状态
  public void setAnonymous(boolean anonymous) {
    this.anonymous = anonymous;
  }


//执行
  public Reply execute() {
    Reply reply = getReply();

    String username = getParameter();//取得用户名
    username = username.toLowerCase();//转成小写字母

    Session session = getConnection().getSession();//取得记录用户信息的session
 //获取该用户的登录状态
    LoginState loginState = (LoginState) session.getAttribute(SessionAttributeName.LOGIN_STATE);
    if (loginState != null) {//重复登录情况处理
      log.debug("Already logged in user submits USER command again");
      reply.setCode("503");
      reply.setText("Already logged in.");
      return reply;
    }

    //将原来的用户名信息移除
    session.removeAttribute(SessionAttributeName.USERNAME);

//用户名为空，不合法用户
    if (username.length() == 0) {
      log.debug("Invalid syntax of submitted username");
      reply.setCode("501");
      reply.setText("Send your user name.");
      return reply;
    }

     //如果用户名为anonymous且不是匿名登录，判定不合法登录
    if (username.equals("anonymous") && !anonymous) {
      log.debug("Anonymous login is disabled");
      reply.setCode("332");
      reply.setText("Anonymous login disabled, need account for login.");
      return reply;
    }

    //将新用户登录名写入到session中
    session.setAttribute(SessionAttributeName.USERNAME, username);
    log.debug("Accepted username: "+username);

    if (username.equals("anonymous")) {//匿名登录，让用户输入其e-mail地址作为密码
      reply.setCode("331");
      reply.setText("Guest login okay, send your complete e-mail address as password.");
      return reply;
    }

    reply.setCode("331");
    reply.setText("User name okay, need password.");//提示用户输入密码
    return reply;
  }
}
