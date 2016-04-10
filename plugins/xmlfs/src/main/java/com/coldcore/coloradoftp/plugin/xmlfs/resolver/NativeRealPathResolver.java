package com.coldcore.coloradoftp.plugin.xmlfs.resolver;

import com.coldcore.coloradoftp.plugin.xmlfs.UserHome;
import com.coldcore.coloradoftp.plugin.xmlfs.VirtualFolder;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * Uses java.io.File to perform virtual to real path mappings.
 */

/**
 * 这个类实现将用户输入的虚拟路径转换成硬盘上对应真实路径
 * 使用了java.io.File 类实现虚拟到真实路径的映射
 */

public class NativeRealPathResolver implements RealPathResolver {

    private static Logger log = Logger.getLogger(NativeRealPathResolver.class);


    /**
     * 将用户输入的文件/文件夹对应的虚拟路径转化成真实文件系统的对应路径
     * 当用户通过FTP客户端输入一个目录或文件,这个类必须保证将其转化成硬盘上对应的真实路径
     *
     * @param path 虚拟路径(绝对路径形式)
     * @param home 用户home
     * @return 硬盘上的绝对路径, 如果不存在则返回null
     */
    public String virtualPathToReal(String path, UserHome home) {
        if (path == null || path.length() == 0 || !path.startsWith("/")) {
            log.debug("Virtual path [" + path + "] cannot be converted to real");
            return null;
        }

    /* First we must determine where input points to. It can point to a root folder,
     * virtual folder, subfolder in user's home path or to a file in user's home path.
     * Then we must check if the path input points to exists on HDD.
     */
        /*首先我们必须确定输入指向的点.它可能是一个文件,文件夹或则是一个目录.
          下面分别对这几种情况进行讨论,如果是一个文件,那么将路径拼接出来以后也可以直接判断
          如果是跟目录,也可以直接进判断.如果是一个目录,那么就需要分情况进行讨论了.
          下面的判断都借助了UserHome对象中记录了用户虚拟跟路径"/"对应的硬盘上的实际路径
        */

        //Points to a file in user home path
        //判断是否是用户home下的一个文件
        File file = new File(home.getPath() + "/" + path.substring(1));
        if (file.exists() && file.isFile()) {
            log.debug("Virtual path [" + path + "] converted to real [" + file.getAbsolutePath() + "]");
            return file.getAbsolutePath();
        }

        //Points to a root folder
        //指向根文件夹
        if (path.equals("/")) {
            file = new File(home.getPath());
            if (file.exists() && file.isDirectory()) {
                log.debug("Virtual path [" + path + "] converted to real [" + file.getAbsolutePath() + "]");
                return file.getAbsolutePath();
            }
            log.warn("User home does not exist: " + file.getAbsolutePath());
            return null;
        }

        //First folder is a virtual folder or sub folder?
        String firstFolderPath = null;
        String firstFolder = path.substring(1);//表示该虚拟路径下第一个目录名称
        if (firstFolder.indexOf("/") != -1) {
            firstFolder = firstFolder.substring(0, firstFolder.indexOf("/"));
        }

        //这里其实有一个文件夹和目录理解的问题.
        //指向一个目录的情况
        VirtualFolder folder = home.getVirtualFolder(firstFolder);
        if (folder != null) {//第一个目录就是一个虚拟文件夹
            //First folder is a virtual folder
            firstFolderPath = folder.getPath();
        } else {
            //First folder is a sub folder or file in user's home path
            //第一个目录是子目录或文件的情况
            file = new File(home.getPath());//将用户相对真实文件系统的根目录拿出来,并且列出其中所有的目录,然后再与我们取得的目录进行对比
            if (file.exists() && file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    if (f.getName().equals(firstFolder) && f.isDirectory()) {
                        firstFolderPath = f.getAbsolutePath();//拿到的是这个目录在硬盘上对应的实际路径
                    }
                }
            }
        }
        //该路径在硬盘上不存在的情况
        if (firstFolderPath == null) {
            log.debug("Virtual path [" + path + "], first folder not found");
            return null;
        }

        //Test if path exists on a hard drive
        //拼接出这个目录在硬盘上的路径,然后再利用File对象进行一次判断
        String relative = path.substring(1 + firstFolder.length());
        String hddPath = firstFolderPath + "/" + relative;
        file = new File(hddPath);
        if (file.exists()) {
            log.debug("Virtual path [" + path + "] converted to real [" + file.getAbsolutePath() + "]");
            return file.getAbsolutePath();
        }

        log.debug("Virtual path [" + path + "], real path does not exist: " + file.getAbsolutePath());
        return null;
    }

}