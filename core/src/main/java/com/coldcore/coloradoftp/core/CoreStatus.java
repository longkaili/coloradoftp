package com.coldcore.coloradoftp.core;

/**
 * Server status.
 *
 *
 * ColoradoFTP - The Open Source FTP Server (http://cftp.coldcore.com)
 */

/*
   定义了服务器的几个状态
*/
public enum CoreStatus {
  STOPPED,  //Stopped　停止
  RUNNING,  //Running　运行
  POISONED  //Shutting down and waiting for all connections to disconnect　等待停止状态(此状态时不接受新连接)
}
