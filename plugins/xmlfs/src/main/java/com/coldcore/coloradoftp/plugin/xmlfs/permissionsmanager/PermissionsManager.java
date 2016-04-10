package com.coldcore.coloradoftp.plugin.xmlfs.permissionsmanager;

import com.coldcore.coloradoftp.plugin.xmlfs.UserHome;
import com.coldcore.coloradoftp.plugin.xmlfs.adapter.FileAdapter;

/**
 * This manager deals with directories permissions (directory properties).
 */

/**
 * 控制目录权限的类
 * 主要判断用户对给定的文件和目录有无操作的权限
 * 权限包括:能否访问,能否重命名,能否更改,能否创建等
 */

public interface PermissionsManager {

    public void setFileAdapter(FileAdapter fileAdapter);

    /** Test if directory access is allowed.
     * Note that this method does not work with paths ending with file separator.
     * @param dirname Directory name (absolute form, real path, proper format)
     * @param home User home
     * @return TRUE if access is allowed, FALSE otherwise
     */
    /**
     * 判断用户是否有权限进入某个路径
     *
     * @param dirname 路径名,绝对路径形式
     * @param home    用户的根目录
     * @return TRUE 如果用户有进入的权限,FALSE 没有权限
     */
    public boolean canAccessDirectory(String dirname, UserHome home);


    /**
     * Test if file access is allowed.
     * Note that this method does not work with paths ending with file separator.
     *
     * @param filename File name (absolute form, real path, proper format)
     * @param home     User home
     * @return TRUE if access is allowed, FALSE otherwise
     */
    /**
     * 判断用户是否有访问某个文件的权限
     * @param filename 文件名(绝对路径根式)
     * @param home 用户的根目录
     * @return TRUE 如果有权限,FALSE 如果没有权限
     * */
    public boolean canAccessFile(String filename, UserHome home);


    /**
     * Test if directory listing is allowed.
     * Note that this method does not work with paths ending with file separator.
     *
     * @param dirname Directory name (absolute form, real path, proper format)
     * @param home    User home
     * @return TRUE if access is allowed, FALSE otherwise
     */
    /**
     * 判断是否可以列出传入的路径
     * @param dirname 目录名
     * @param home 用户的home
     * @return  TRUE如果有权限,FALSE 如果没有权限
     * */
    public boolean canListDirectory(String dirname, UserHome home);


    /**
     * Test if file listing is allowed.
     * Note that this method does not work with paths ending with file separator.
     *
     * @param filename File name (absolute form, real path, proper format)
     * @param home     User home
     * @return TRUE if access is allowed, FALSE otherwise
     */
    /**
     * 判断是否可以列出文件名
     * @param filename 文件名
     * @param home 用户home
     * @return TRUE 如果有权限,FALSE 如果没有权限
     * */
    public boolean canListFile(String filename, UserHome home);


    /**
     * Test if directory delete is allowed.
     * Note that this method may not work with paths ending with file separator.
     *
     * @param dirname Directory name (absolute form, real path, proper format)
     * @param home    User home
     * @return TRUE if access is allowed, FALSE otherwise
     */
    public boolean canDeleteDirectory(String dirname, UserHome home);


    /**
     * Test if file delete is allowed.
     * Note that this method may not work with paths ending with file separator.
     *
     * @param filename File name (absolute form, real path, proper format)
     * @param home     User home
     * @return TRUE if access is allowed, FALSE otherwise
     */
    /**
     * 判断能否删除文件
     * @param filename 文件名
     * @param  home 用户的home
     * @return  TRUE 如果可以,FALSE 如果不可以
     * */
    public boolean canDeleteFile(String filename, UserHome home);


    /**
     * Test if directory rename is allowed.
     * Note that this method may not work with paths ending with file separator.
     *
     * @param dirname Directory name (absolute form, real path, proper format)
     * @param home    User home
     * @return TRUE if access is allowed, FALSE otherwise
     */
    /**
     * 判断能否重命名目录
     * @param  dirname 目录名
     * @param home    用户home
     * @return  TRUE 如果有权限,FALSE 如果没有权限
     * */
    public boolean canRenameDirectory(String dirname, UserHome home);


    /**
     * Test if file rename is allowed.
     * Note that this method may not work with paths ending with file separator.
     *
     * @param filename File name (absolute form, real path, proper format)
     * @param home     User home
     * @return TRUE if access is allowed, FALSE otherwise
     */
    /**
     * 判断能否重命名文件
     * @param filename 文件名
     * @param home   用户home
     * @return TRUE 如果有权限,FALSE 如果没有权限
     * */
    public boolean canRenameFile(String filename, UserHome home);


    /**
     * Test if directory create is allowed.
     * Note that this method may not work with paths ending with file separator.
     *
     * @param dirname Directory name (absolute form, real path, proper format)
     * @param home    User home
     * @return TRUE if access is allowed, FALSE otherwise
     */
    /**
     * 判断能否创建目录
     * @param dirname 目录名(绝对路径形式)
     * @param  home   用户的home
     * @return TRUE 如果有权限,FALSE 如果没有权限
     */
    public boolean canCreateDirectory(String dirname, UserHome home);


    /**
     * Test if file create is allowed.
     * Note that this method may not work with paths ending with file separator.
     *
     * @param filename File name (absolute form, real path, proper format)
     * @param home     User home
     * @return TRUE if access is allowed, FALSE otherwise
     */
    /**
     *判断能否创建文件
     * @param  filename 文件名(绝对路径形式)
     * @param  home 用户的home
     * @return  TRUE 如果有权限,FALSE 如果没有权限
     * */
    public boolean canCreateFile(String filename, UserHome home);


    /**
     * Test if file append is allowed.
     * Note that this method may not work with paths ending with file separator.
     *
     * @param filename File name (absolute form, real path, proper format)
     * @param home     User home
     * @return TRUE if access is allowed, FALSE otherwise
     */
    /**
     * 判断能否附加文件
     * @param filename 文件名(绝对路径形式)
     * @param  home    用户home
     * @return TRUE 如果有权限,FALSE 如果没有权限
     * */
    public boolean canAppendFile(String filename, UserHome home);


    /**
     * Test if file overwrite is allowed.
     * Note that this method may not work with paths ending with file separator.
     *
     * @param filename File name (absolute form, real path, proper format)
     * @param home     User home
     * @return TRUE if access is allowed, FALSE otherwise
     */
    /**
     * 判断用户是否能够重写一个文件
     * @param filename 文件名
     * @param home 用户home
     * @return  TRUE 如果有权限,FALSE 如果没有
     * */
    public boolean canOverwriteFile(String filename, UserHome home);
}