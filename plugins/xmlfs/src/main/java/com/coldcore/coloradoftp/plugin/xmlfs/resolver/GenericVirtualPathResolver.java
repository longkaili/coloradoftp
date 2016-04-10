package com.coldcore.coloradoftp.plugin.xmlfs.resolver;

import com.coldcore.coloradoftp.plugin.xmlfs.UserHome;
import com.coldcore.coloradoftp.session.Session;
import com.coldcore.coloradoftp.session.SessionAttributeName;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Generic resolver.
 * This class uses unix style: "/" is a root directory and "/" is a file separator.
 */

/**
 * 将相对的虚拟路径转换成绝对的虚拟路径并且在上面执行一些操作(返回父路径,
 * 判断是文件还是文件夹,获得用户当前路径等).这个类使用了unix风格的文件分隔符"/"
 */

public class GenericVirtualPathResolver implements VirtualPathResolver {

    private static Logger log = Logger.getLogger(GenericVirtualPathResolver.class);


    /**
     * 虚拟相对路径转绝对路径
     *
     * @param path        传入的相对路径
     * @param userSession 记录用户信息的session
     * @return 绝对路径
     */
    public String virtualPathToAbsolute(String path, Session userSession) {

        //传入路径为空情况
        if (path == null || path.length() == 0) {
            log.debug("Virtual path [" + path + "] cannot be converted to absolute");
            return null;
        }

        String apath = path;
        //传入的是绝对路径,此时需要去除多余的分隔符,并且处理路径中包含".."的情况
        if (apath.startsWith("/")) {

            //去除路径最后面的分隔符
            while (apath.endsWith("/")) {
                apath = apath.substring(0, apath.length() - 1);
            }
            //传入的是根目录
            if (apath.length() == 0) {
                apath = "/";
            }
            apath = fixVirtualParentRefs(apath);
            log.debug("Virtual path [" + path + "] converted to absolute [" + apath + "]");
            return apath;
        }

        //下面是处理传入的是相对路径的情况
        //此时需要先取得用户与当前的路径,然后拼凑出绝对路径,然后再按上面的方式处理
        while (apath.endsWith("/")) {
            apath = apath.substring(0, apath.length() - 1);
        }

        //从Session中取得用户当前的路径
        String curDir = (String) userSession.getAttribute(SessionAttributeName.CURRENT_DIRECTORY);
        if (curDir == null || curDir.equals("/")) {
            apath = "/" + apath;
            apath = fixVirtualParentRefs(apath);
            log.debug("Virtual path [" + path + "] converted to absolute [" + apath + "]");
            return apath;
        }

        apath = curDir + "/" + apath;
        apath = fixVirtualParentRefs(apath);
        log.debug("Virtual path [" + path + "] converted to absolute [" + apath + "]");
        return apath;
    }


    /**
     * Normalizes(规范) path by removing references to parent dirs from a virtual path and removing multiple file separators.
     * WARNING! The path may contain ".." (references to the parent dir). This is a serious security issue because
     * user may go outside of his root dir. This method correctly converts the path to the same path but without "..".
     *
     * @param path Absolute virtual file/folder name
     * @return Absolute virtual file/folder name (does not end with file separator) or NULL if cannot be converted
     */
    /**
     * 通过移除多余的文件分隔符及去除路径中的".."对传入的绝对路径进行规范
     *
     * @param path 传入的绝对路径
     * @return 经过规范以后的绝对路径, 如果传入的路径转换失败, 则返回null
     */
    protected String fixVirtualParentRefs(String path) {
        if (path == null || !path.startsWith("/")) {
            return path;
        }

        //Add all items (names separated by "/") to the list
        List<String> items = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(path, "/");
        while (st.hasMoreTokens()) {
            items.add(st.nextToken());
        }

        //Remove all ".." items and items (folders) that stand in front of them
        for (int z = 0; z < items.size(); z++) {
            String item = items.get(z);
            if (item.trim().equals("..")) {
                //Remove this ".." and also remove the previous folder (if it exists)
                items.remove(z);
                if (z > 0) {
                    items.remove(--z);
                }
                z--;
            }
        }

        //Assemble the path again from the items (now there are no "..")
        path = "";
        for (String item : items) {
            path += "/" + item;
        }
        if (path.length() == 0) {
            path = "/";
        }

        return path;
    }


    /**
     * 判断传入的路径是否是虚拟文件夹
     *
     * @param path 传入的路径
     * @param home 记录用户路径信息的home
     * @return TRUE如果是, FALSE不是
     */
    public boolean isVirtualFolder(String path, UserHome home) {

        //如果是空路径,不以"/"开头,以"/"结尾,则默认不是文件夹
        if (path == null || path.length() == 0 || !path.startsWith("/") || path.endsWith("/")) {
            return false;
        }
        String name = path.substring(1);//文件夹下面就是具体的文件了,所以这样可以取得文件夹的名字
        //取得名字以后,通过UserHome中的相关方法进行判断
        return home.getVirtualFolder(name) != null;
    }


    /**
     * 取得传入路径的父路径,session中一般是记录了用户的当前所在路径的,
     * 如果为空的话,则认为用户处在其根目录下
     *
     * @param path 传入的路径
     * @return 取得父路径
     */
    public String getVirtualParent(String path) {
        if (path == null || path.length() == 0 || !path.startsWith("/") || path.endsWith("/")) {
            return path;
        }
        String apath = path.substring(0, path.lastIndexOf("/"));
        if (apath.length() == 0) {
            apath = "/";
        }
        log.debug("Parent of [" + path + "] is [" + apath + "]");
        return apath;
    }


    /**
     * 取得当前用户所在的虚拟路径
     *
     * @param userSession 用户的Session对象
     * @return 用户当前所处的虚拟路径
     */
    public String getCurrentVirtualDirectory(Session userSession) {
        String curDir = (String) userSession.getAttribute(SessionAttributeName.CURRENT_DIRECTORY);
        if (curDir == null) {
            curDir = "/";
            userSession.setAttribute(SessionAttributeName.CURRENT_DIRECTORY, curDir);
        }
        log.debug("User current directory: " + curDir);
        return curDir;
    }


    //默认的文件分隔符是"/"
    public String getVirtualFileSeparator() {
        return "/";
    }
}