package com.coldcore.coloradoftp.plugin.xmlfs.adapter;

/**
 * File Adapter(适配器) containing several file related operations.
 * This is required to abstract from a hard drive. All the input and output
 * parameters relate to an actual underlying file system (eg drive).
 */

/**
 * 路径格式适配器
 * 主要适配路径分隔符
 */

public interface FileAdapter {

    /** Similar to:
     *    File.separator = /
     *    File.separator = \
     * @return File separator as used by an underlying real file system (eg drive)
     */
    /**
     * 获得当前真实文件系统对应的分隔符
     *
     * @return 真实文件系统使用的分隔符
     */
    public String getSeparator();


    /** Similar to:
     *    File("/foo/bar").getAbsolutePath() = /foo/bar
     *    File("/foo/bar/").getAbsolutePath() = /foo/bar
     * @param path Absolute path, may not have proper file separators or may have extra spaces as this value
     *             may be read from a configuration file and supplied as is.
     * @return Absolute path with proper file separators (matching getSeparator), without a
     *         file separator at the end etc
     */
    /**
     * 将传入的路径转换成合适格式的路径
     *
     * @param path 传入的绝对路径,可能包含空格或没有使用合适的分隔符
     * @return 合适的路径
     */
    public String normalizePath(String path);


    /** Similar to:
     *    File("/foo/bar").getParentFile().getAbsolutePath() = /foo
     *    File("/foo").getParentFile().getAbsolutePath() = /
     * @param path Absolute path, proper format, with proper file separators (output of normalizePath)
     * @return Absolute path of a parent folder or a root if no parent found
     */
    /**
     * 获得传入路径的父路径
     *
     * @param path 经过格式化后的路径
     * @return 给定路径的父路径, 如果传入的是根路径, 返回其本身
     */
    public String getParentPath(String path);

}