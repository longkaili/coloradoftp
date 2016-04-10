package com.coldcore.coloradoftp.connection;

/**
 * Data connection transfer mode.
 * Explains a reason what's for users request data transfers.
 *
 *
 * ColoradoFTP - The Open Source FTP Server (http://cftp.coldcore.com)
 */

/**数据连接传输模式*/
public enum DataConnectionMode {
  //从用户中读取数据并写入到文件中
  STOR, //Read data from the user and save it into file
  //同上，只是在传输的最后回复250
  STOU,//Same as STOR, only send "250" reply at the end of the transfer
  // 从文件中读取数据传输给用户
  RETR, //Read data from file and send it to user
  //发送服务器的目录给用户
  LIST  //Send directory content to user
}
