package com.coldcore.coloradoftp.command.impl;

import com.coldcore.coloradoftp.command.Command;
import com.coldcore.coloradoftp.command.Reply;
import com.coldcore.coloradoftp.connection.ControlConnection;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;
import com.coldcore.coloradoftp.session.DataOpenerType;
import com.coldcore.coloradoftp.session.LoginState;
import com.coldcore.coloradoftp.session.Session;
import com.coldcore.coloradoftp.session.SessionAttributeName;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @see com.coldcore.coloradoftp.command.Command
 *
 * Base class with few helper methods.
 */
/**实现了部分方法的抽象命令类*/
abstract public class AbstractCommand implements Command {

  protected String name;//命令的名字
  protected String parameter;//命令的参数
  protected ControlConnection controlConnection;//命令对应的控制连接(命令的发出者)
  private Reply reply; //Via getter only!//命令对应的回复对象


  //获得命令对应的回复对象
  protected Reply getReply() {
    if (reply == null) {
      reply = (Reply) ObjectFactory.getObject(ObjectName.REPLY);
      reply.setCommand(this);
    }
    return reply;
  }


  /** Test if user is loggen in.
   *  If user is not logged in this method fills the internal reply with a default error message.
   * @return TRUE if user is logged in, FALSE if user is not logged in
   */
  //测试用户是否已经登录，若未登录则发送错误的信息
  protected boolean testLogin() {
    Session session = getConnection().getSession();
    LoginState loginState = (LoginState) session.getAttribute(SessionAttributeName.LOGIN_STATE);
    if (loginState != null) return true;
    Reply reply = getReply();
    reply.setCode("530");
    reply.setText("Not logged in.");
    return false;
  }


  /** Prepares everything for a new data connection.
   * If failed then this method fills the internal reply with a default error message.
   * It is recommended to call this method last because it is not easy to reverse changes this
   * method applies. 
   * @return TRUE if ready for a new data connection, FALSE otherwise
   */
 //如果受到的是关于数据连接的命令，这个方法为新建一个数据连接做好准备
 //分为主动型和被动型的数据连接
  protected boolean prepareForDataConnection() {
    Session session = getConnection().getSession();
  //获得数据连接类型
    DataOpenerType dtype = (DataOpenerType) session.getAttribute(SessionAttributeName.DATA_OPENER_TYPE);
    if (dtype == null) {//如果数据连接的类型为空，则此次连接无效
      Reply reply = getReply();
      reply.setCode("425");
      reply.setText("Can't open data connection.");
      return false;
    }

    //PASV is active only once as the connection is removed from the listeners set
    //主动连接类型，将类型信息从session中去除
    if (dtype == DataOpenerType.PASV) {
      session.removeAttribute(SessionAttributeName.DATA_OPENER_TYPE);
      return true;
    }

    //PORT must activate data connection initiator in the control connection
    //被动类型的数据连接，由控制连接主动生成一个控制连接
    if (dtype == DataOpenerType.PORT) {

      //Byte marker before "150" reply for a data connection initiator
      //??
      session.setAttribute(SessionAttributeName.BYTE_MARKER_150_REPLY, controlConnection.getBytesWrote());

      //控制连接创建一个数据连接
      controlConnection.getDataConnectionInitiator().activate();
      return true;
    }

    throw new RuntimeException("BUG: Unknown data opener type provided");
  }


  /** Check syntax
   *  @param str String to check
   *  @param regexp Regular expression that defines syntax rules
   */
  //按传入的正则表达式检查传入的字符串是否合法
  protected boolean checkRegExp(String str, String regexp) {
    Pattern pattern = Pattern.compile(regexp);
    Matcher matcher = pattern.matcher(str);
    return matcher.matches();
  }


   //是否在InterruptedState状态执行
  public boolean processInInterruptState() {
    return false;
  }

  //是否能清除服务器InterruptState
  public boolean canClearInterruptState() {
    return false;
  }

  //返回命令名字，总是大写形式
  public String getName() {
    if (name == null) return null;
    return name.trim().toUpperCase(); //Return in uppercase
  }


  //设定用户命令
  public void setName(String name) {
    this.name = name;
  }


  //返回参数
  public String getParameter() {
    if (parameter == null) return ""; //Do not return NULL
    return parameter.trim();
  }


  public void setParameter(String parameter) {
    this.parameter = parameter;
  }


  public void setConnection(ControlConnection connection) {
    controlConnection = connection;
  }


  public ControlConnection getConnection() {
    return controlConnection;
  }


  public Reply executeOnParent(Command parent) {
    return null;  
  }
  
  public String getHelpText() {
	  return "";
  }
  
  public String getShortHelpText() {
	  return getHelpText();
  }
  
  public String getLongHelpText() {
	  return getHelpText();
  }
  

}
