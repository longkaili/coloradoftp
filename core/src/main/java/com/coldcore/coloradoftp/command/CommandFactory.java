package com.coldcore.coloradoftp.command;

import java.util.Set;

/**
 * Commands factory (creates all available commands).
 *
 * Creates a command based on supplied user input. If a command cannot be created it must
 * be substituted with one of the system commands carrying a proper error message.
 *
 * Control connection relies on this class to get a command object for submitted user input,
 * it then feeds a freshly created command to a command processor for further execution.
 *
 *
 * ColoradoFTP - The Open Source FTP Server (http://cftp.coldcore.com)
 */
/**
　　命令工厂类，根据用户的输入创建相应的命令对象
    如果没有对应的命令对象，则使用系统命令对象返回恰当的错误信息
*/
public interface CommandFactory {

  /** List all available command names
   * @return Names of commands (copy of the original set)
   */
  //返回所有可以执行命令的名字
  public Set<String> listNames();


  /** Create command object
   * @param input User input
   * @return Command or command with failed message (never NULL)
   */
   //根据用户的输入，创建相应的命令对象或带有失败信息的命令对象
  public Command create(String input);
}
