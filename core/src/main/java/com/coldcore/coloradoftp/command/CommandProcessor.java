package com.coldcore.coloradoftp.command;

/**
 * Processes user input.
 *
 * This class executes user commands and sends replies.
 *
 * When dealing with exceptions from command executions, this class must
 * not throw exceptions, instead it must send to user replies with error messages.
 * Note that error from a filesystem class are not always errors producing
 * default LOCAL ERROR reply. That is because filesystem is unable to
 * post replies and it communicates back using a special kind of exceptions.
 *
 *
 * ColoradoFTP - The Open Source FTP Server (http://cftp.coldcore.com)
 */
/**执行用户命令类，对于执行命令过程中出现的异常，不能直接抛出，而是要返回给用户相应的回复*/
public interface CommandProcessor {

  /** Execute the command
   * @param command Command
   */
  public void execute(Command command);
}
