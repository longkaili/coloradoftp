package com.coldcore.coloradoftp.plugin.xmlfs.resolver;

import com.coldcore.coloradoftp.plugin.xmlfs.UserHome;
import com.coldcore.coloradoftp.session.Session;

/**
 * Converts virtual path to an absolute virtual path and performs some operations on it.
 */
/**
 * 将相对的虚拟路径转换成绝对的虚拟路径并且在上面执行一些操作(返回父路径,判断是文件还是文件夹,获得用户当前路径等)
 * */
public interface VirtualPathResolver {

  /** Convert virtual path name (user input as is) to absolute form.
   * When user submits a directory or file name via(通过) FTP client this method must ensure that
   * it is in an absolute form.
   * @param path Path to convert (as FTP client sumbits)
   * @param userSession User session
   * @return Absolute file/folder name (which does not end with file separator) or NULL if cannot be converted
   */
  /**
   * 将用户输入的相对路径转换成绝对路径
   * 当用户通过FTP客户端传入一个相对路径时,这个方法要保证将其转换成绝对路径
   * @param path 需要被转换的Path
   * @param userSession 记录用户信息的session
   * @return  文件或文件夹对应的绝对路径,如果转换不成功,返回null
   * */
  public String virtualPathToAbsolute(String path, Session userSession);


  /** Test if virtual path (user input as is) is a virtual folder mounted to users home.
   * Note that this method may not work with paths ending with file separator.
   * @param path Absolute path (as FTP client sumbits, in absolute form)
   * @param home User home
   * @return TRUE if path points to a virtual folder (and not further), FALSE otherwise
   */
  /**
   * 判断一个虚拟的路径是否是用户home下的一个虚拟文件夹
   * @param  path 绝对路径
   * @param home 用户的home
   * @return  TRUE 如果是,FALSE 如果不是
   * */
  public boolean isVirtualFolder(String path, UserHome home);


  /** Get virtual parent path.
   * Note that this method may not work with paths ending with file separator.
   * If a path already is a root folder then the root folder will be returned.
   * @param path Absolute path (as FTP client sumbits, in absolute form)
   * @return Parent path (absolute form)
   */
  /**
   * 取得一个路径的父路径,如果传入的是跟目录,返回其本身
   * @param path 一个绝对路径
   * @return 父路径
   * */
  public String getVirtualParent(String path);


  /** Get user current virtual directory.
   * @param userSession User session
   * @return Current directory (absolute form)
   */
  /**
   * 获得用户当前所在的路径
   * @param userSession 记录用户信息的session
   * @return 当前路径 (绝对路径形式)
   * */
  public String getCurrentVirtualDirectory(Session userSession);


  /** Return virtual file separator.
   * @return File separator (may have more than 1 character, however, this is not recommended)
   */
  /**返回虚拟文件分隔符
   * @return 返回文件分隔符,可能不止一个字符*/
  public String getVirtualFileSeparator();
}