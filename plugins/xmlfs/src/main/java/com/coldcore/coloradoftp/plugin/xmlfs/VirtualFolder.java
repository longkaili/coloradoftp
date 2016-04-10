package com.coldcore.coloradoftp.plugin.xmlfs;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Virtual folder.
 * <p/>
 * Virtual folders mount directories from anywhere on hard drive to user's
 * home directory. User sees the name of the virtual folder in his/her FTP list
 * which is the entry point to the mounted directory itself.
 */

/**
 * Virtual folder类
 * 虚拟文件夹类将真实文件系统中的任何路径转换成用户的根目录.
 * 用户在FTP list上看到的就是这些虚拟的文件夹
 * */
public class VirtualFolder {

    protected String           name;//虚拟文件夹的名字
    protected String           path;//虚拟文件夹对应的路径,绝对路径

    //路径对应的权限集合,每个路径都会对应一个DirProperty对象
    protected Set<DirProperty> properties;


    public VirtualFolder() {
        properties = new LinkedHashSet<DirProperty>();
    }


    /** Get the name
     * @return Virtual folder name
     */
    public String getName() {
        return name;
    }


    /** Set the name
     * @param name Virtual folder name
     */
    public void setName(String name) {
        this.name = name;
    }


    /** Get mounted path
     * @return Absolute path name
     */
    public String getPath() {
        return path;
    }


    /** Set mounted path
     * @param absPath Absolute path name
     */
    public void setPath(String absPath) {
        path = absPath;
    }


    /** Add properties that apply to this virtual folder and its content
     * @param set Directory properties
     */
    public void addProperties(Set<DirProperty> set) {
        properties.addAll(set);
    }


    /** Get properties that apply to this virtual folder and its content
     * @return Directory properties (read only!)
     */
    public Set<DirProperty> getProperties() {
        return properties;
    }
}