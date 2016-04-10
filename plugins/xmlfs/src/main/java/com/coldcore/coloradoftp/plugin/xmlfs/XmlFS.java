package com.coldcore.coloradoftp.plugin.xmlfs;

import com.coldcore.coloradoftp.plugin.xmlfs.adapter.FileAdapter;
import com.coldcore.coloradoftp.plugin.xmlfs.adapter.NativeFileAdapter;
import com.coldcore.coloradoftp.plugin.xmlfs.parser.ConfigurationParser;
import com.coldcore.coloradoftp.plugin.xmlfs.parser.ManualConfigurationParser;
import com.coldcore.coloradoftp.plugin.xmlfs.parser.ParsingException;
import com.coldcore.coloradoftp.plugin.xmlfs.permissionsmanager.GenericPermissionsManager;
import com.coldcore.coloradoftp.plugin.xmlfs.permissionsmanager.PermissionsManager;
import com.coldcore.coloradoftp.plugin.xmlfs.resolver.GenericVirtualPathResolver;
import com.coldcore.coloradoftp.plugin.xmlfs.resolver.NativeRealPathResolver;
import com.coldcore.coloradoftp.plugin.xmlfs.resolver.RealPathResolver;
import com.coldcore.coloradoftp.plugin.xmlfs.resolver.VirtualPathResolver;
import com.coldcore.coloradoftp.session.Session;
import com.coldcore.coloradoftp.session.SessionAttributeName;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Set;

/**
 * Main class.
 * <p/>
 * By default it maps to a HDD to support the behaviour of the Hard File System plug-in.
 * <p/>
 * Every user starts in his own "/" root directory. This appears as a root directory of the server but
 * it can be just anywhere in the actual filesystem. User cannot go outside of his/her home directory,
 * but virtual folders can be mounted to user's home allowing user to travel anywhere withing the actual
 * filesystem.
 */
/**
 * 这个类主要是用来支持文件系统
 * 每个用户都会有自己的根目录"/",但这只是服务器上的根路径,在实际文件系统中,这可以是任意的路径.
 * 每个用户只能在他自己的根目录下操作
 * */
public class XmlFS {

    private static Logger log = Logger.getLogger(XmlFS.class);

    //用户权限控制对象
    protected PermissionsManager permissionsManager;

    //配置文件解析对象(?)
    protected ConfigurationParser configurationParser;

    //将用户输入的相对路径转换成绝对路径,并进行一些操作的对象
    protected VirtualPathResolver virtualPathResolver;

    //将虚拟文件系统中的绝对路径转化成真实文件系统的路径
    protected RealPathResolver realPathResolver;

    //接入文件系统的用户集合
    protected Set<User> users;

    //路径格式过滤器
    protected FileAdapter fileAdapter;


    public XmlFS() {
      virtualPathResolver = new GenericVirtualPathResolver();
      realPathResolver = new NativeRealPathResolver();
      permissionsManager = new GenericPermissionsManager();
      configurationParser = new ManualConfigurationParser();
      fileAdapter = new NativeFileAdapter();
    }


    /**
     * Initialization
     * @param doc Configuration XML
     */
    /**
     * 初始化
     * @param doc  可以doc对象代表一个XML的配置文件
     * */
    public void initialize(Document doc) throws ParsingException {
        changeFileAdapter();
        configurationParser.initialize(doc);
        afterInitialization();
    }


    /**
     * Initialization
     * @param filename Path to configuration XML
     */
    /**初始化
     * @param filename XML配置文件的路径*/
    public void initialize(String filename) throws ParsingException, FileNotFoundException {
        changeFileAdapter();
        configurationParser.initialize(filename);
        afterInitialization();
    }


    /**
     * Initialization
     * @param in Stream with configuration XML
     */
    /**
     * 初始化
     * @param in XML配置文件对应的输入流
     * */
    public void initialize(InputStream in) throws ParsingException {
        changeFileAdapter();
        configurationParser.initialize(in);
        afterInitialization();
    }


    /**
     * Executed after configuration has been parsed(解析)
     */
    /**
     * 当配置文件被解析以后
     * 利用这个类创建用户对象
     * */
    protected void afterInitialization() throws ParsingException {
        users = configurationParser.createUsers();
    }


    /**
     * Changes file adapters of the underlying objects
     */
    /**
     * 改变文件适配器
     * */
    protected void changeFileAdapter() {
      permissionsManager.setFileAdapter(fileAdapter);
      configurationParser.setFileAdapter(fileAdapter);
    }


    /**
     * 设定文件适配器
     * */
    public void setFileAdapter(FileAdapter fileAdapter) {
      this.fileAdapter = fileAdapter;
    }


    public FileAdapter getFileAdapter() {
      return fileAdapter;
    }


    public PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }


    public void setPermissionsManager(PermissionsManager permissionsManager) {
        this.permissionsManager = permissionsManager;
    }


    public VirtualPathResolver getVirtualPathResolver() {
        return virtualPathResolver;
    }


    public void setVirtualPathResolver(VirtualPathResolver virtualPathResolver) {
        this.virtualPathResolver = virtualPathResolver;
    }


    public RealPathResolver getRealPathResolver() {
        return realPathResolver;
    }


    public void setRealPathResolver(RealPathResolver realPathResolver) {
        this.realPathResolver = realPathResolver;
    }


    public ConfigurationParser getConfigurationParser() {
        return configurationParser;
    }


    public void setConfigurationParser(ConfigurationParser configurationParser) {
        this.configurationParser = configurationParser;
    }


    /**
     * Find user (by username, not case sensitive(不区分大小写))
     *
     * @param userSession User sessison
     * @return User object (or default user object) or NULL if user not found
     */
    /**
     * 寻找用户
     * @param userSession 传入的用户对应的Session对象,实际上是根据用户的名字进行检索
     * @return 如果存在返回对应的User对象,否则返回null
     * */

    public User findUser(Session userSession) {
        String username = (String) userSession.getAttribute(SessionAttributeName.USERNAME);
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) return user;
        }
        //默认用户
        for (User user : users) {
            if (user.isDefault()) return user;
        }
        log.warn("User " + username + " has no filesystem entry");
        return null;
    }


    //返回当前用户的集合
    public Set<User> getUsers() {
        return users;
  }
}
