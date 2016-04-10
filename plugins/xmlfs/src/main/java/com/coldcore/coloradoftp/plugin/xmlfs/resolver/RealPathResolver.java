package com.coldcore.coloradoftp.plugin.xmlfs.resolver;

import com.coldcore.coloradoftp.plugin.xmlfs.UserHome;

/**
 * This class maps a virtual path to a real path on a real file system (eg drive).
 */

/**
 * 这个类实现将用户输入的虚拟路径转换成硬盘上对应真实路径
 * */

public interface RealPathResolver {

  /** Convert folder/file virtual path name (user input as is) to a path on a real file system (eg drive).
   * When user submits a directory or file name via FTP client this method must convert
   * it to a path on a hard drive that corresponds to the virtual path.
   * @param path Absolute path name to convert (as FTP client sumbits, in absolute form)
   * @param home User home
   * @return Absolute path on a hard drive (in proper format and without file separator in the end)
   *         or NULL if cannot be converted
   */
  /**
   * 将用户输入的文件/文件夹对应的虚拟路径转化成真实文件系统的对应路径
   * 当用户通过FTP客户端输入一个目录或文件,这个类必须保证将其转化成硬盘上对应的真实路径
   * @param path 虚拟路径(绝对路径形式)
   * @param home  用户home
   * @return  硬盘上的绝对路径,如果不存在则返回null
   * */
  public String virtualPathToReal(String path, UserHome home);

}