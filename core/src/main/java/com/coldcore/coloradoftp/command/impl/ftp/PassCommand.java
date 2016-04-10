/**
 * Command PASS.
 * See FTP spec for details on the command.
 *
 * This implementation denies all logins but anonymous.
 * Extend this class to perform a passowrd control.
 */
package com.coldcore.coloradoftp.command.impl.ftp;

import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.command.impl.AbstractCommand;
import com.coldcore.coloradoftp.session.LoginState;
import com.coldcore.coloradoftp.session.Session;
import com.coldcore.coloradoftp.session.SessionAttributeName;
import org.apache.log4j.Logger;

/**
   "口令PASS"命令，它的参数是指定用户口令的Telnet字符串.
   此指令紧跟用户名指令，在某些站点它是完成访问控制不可缺少的一步．
　　　该类实现的还不是很完整，可以扩展这个类实现完整的密码控制功能
*/
public class PassCommand extends AbstractCommand {

  private static Logger log = Logger.getLogger(PassCommand.class);
  private String emailRegExp;//用于验证邮箱格式的正则表达式


  public PassCommand() {
    emailRegExp = "([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]*)";
  }


  /** Get email regular expression
   * @return Email regular expression
   */
  public String getEmailRegExp() {
    return emailRegExp;
  }


  /** Set email regular expression
   * @param emailRegExp Email regular expression
   */
  //设定邮箱格式验证正则表达式
  public void setEmailRegExp(String emailRegExp) {
    this.emailRegExp = emailRegExp;
  }

//执行方法体
  public Reply execute() {
    Reply reply = getReply();

    String password = getParameter();

    Session session = getConnection().getSession();
    LoginState loginState = (LoginState) session.getAttribute(SessionAttributeName.LOGIN_STATE);
    if (loginState != null) {//重复登录情况
      log.debug("Already logged in user submits PASS command again");
      reply.setCode("503");
      reply.setText("Already logged in.");
      return reply;
    }

    //取得用户名
    String username = (String) session.getAttribute(SessionAttributeName.USERNAME);
    if (username == null) {//未输入用户名就输入密码情况，让用户先输入用户名
      reply.setCode("503");
      reply.setText("Send your user name.");
      return reply;
    }

    if (password.length() == 0) {//输入密码长度为０情况
      log.debug("Invalid syntax of submitted password");
      reply.setCode("501");
      reply.setText("Send your password.");
      return reply;
    }

    //Anonymous login (password is user's email)
    //匿名登录情况
    if (username.equalsIgnoreCase("anonymous")) {
      if (checkRegExp(password, emailRegExp)) {//输入邮箱格式正确，匿名登录成功
        log.debug("Anonymous login successful for username: "+username+" ("+password+")");
        session.setAttribute(SessionAttributeName.LOGIN_STATE, LoginState.ANONYMOUS);
        session.setAttribute(SessionAttributeName.PASSWORD, password);
        reply.setCode("230");
        reply.setText("User logged in, proceed.");
        return reply;
      } else {//输入邮箱格式不对
        log.debug("Anonymous login failed for username: "+username+" ("+password+")");
        reply.setCode("530");
        reply.setText("Not logged in.");
        return reply;
      }
    }

    //Regular login
    //常规登录
    if (checkLogin(username, password)) {
      log.debug("Login successful for username: "+username);
      session.setAttribute(SessionAttributeName.LOGIN_STATE, LoginState.REGULAR);
     //从安全方面考虑，用户的密码不会写入到session中去
      /* For security reasons password is not stored into the session */
      reply.setCode("230");
      reply.setText("User logged in, proceed.");
      return reply;
    }

    //Login failed
    //登录失败
    log.debug("Login failed for username: "+username);
    reply.setCode("530");
    reply.setText("Not logged in.");
    return reply;
  }


  /** Test user login.
   * This implementation always returns FALSE.
   * @param username Username
   * @param password Password
   * @return TRUE if login is OK, FALSE otherwise
   */
//常规登录的验证，目前没有实现，总是返回false
  protected boolean checkLogin(String username, String password) {
    return false;
  }
}
