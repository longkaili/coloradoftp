package com.coldcore.coloradoftp.plugin.xmlfs;

import com.coldcore.coloradoftp.plugin.xmlfs.adapter.FileAdapter;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * User home.
 * <p/>
 * It is a list of directories and virtual folders user's filesystem contains.
 */

/**
 * User home
 * 记录了用户fileSystem中包含的目录和虚拟文件夹
 */

public class UserHome {

    //目录权限检查,针对的是硬盘上对应的真实路径
    protected Set<DirProperty> properties;

    //用户对应的虚拟文件夹对象
    protected Set<VirtualFolder> folders;

    //用户根目录路径
    protected String path;

    //路径格式检查和转换类(主要是分隔符)
    protected FileAdapter fileAdapter;


    public UserHome(FileAdapter fileAdapter) {
        if (fileAdapter == null) {
            throw new IllegalArgumentException("Invalid file adapter");
        }
        this.fileAdapter = fileAdapter;
        properties = new LinkedHashSet<DirProperty>();
        folders = new HashSet<VirtualFolder>();
    }


    /**
     * Add directory properties
     *
     * @param set Directory properties
     */
    public void addProperties(Set<DirProperty> set) {
        properties.addAll(set);
    }


    /**
     * Get directory properties
     *
     * @return Directory properties (read only!)
     */
    public Set<DirProperty> getProperties() {
        return properties;
    }


    /**
     * Add virtual folders
     *
     * @param set Virtual folders
     */
    public void addFolders(Set<VirtualFolder> set) {
        folders.addAll(set);
    }


    /**
     * Get path to user home folder
     *
     * @return Absolute path
     */
    public String getPath() {
        return path;
    }


    /** Set path to user home folder (converts to proper format)
     * @param absPath Absolute path
     */
    /**
     * 设置用户根目录路径
     * 根目录是绝对路径形式
     */
    public void setPath(String absPath) {
        absPath = fileAdapter.normalizePath(absPath);
        path = absPath;
    }


    /** Get virtual folder by name
     * @param name Virtual folder name
     * @return Virtual folder or NULL
     */
    /**
     * 通过名字获得对应的虚拟文件夹
     *
     * @param name 传入虚拟文件夹得名字
     * @return 返回对应的虚拟文件夹或null
     */
    public VirtualFolder getVirtualFolder(String name) {
        for (VirtualFolder folder : folders) {
            if (folder.getName().equals(name)) {
                return folder;
            }
        }
        return null;
    }


    /**
     * Get virtual folders
     *
     * @return Virtual folders (read only!)
     */
    /**
     * 返回所有的虚拟文件夹
     * */
    public Set<VirtualFolder> getVirtualFolders() {
        return folders;
    }
}