package com.coldcore.coloradoftp.plugin.xmlfs.parser;

import com.coldcore.coloradoftp.plugin.xmlfs.User;
import com.coldcore.coloradoftp.plugin.xmlfs.adapter.FileAdapter;
import org.w3c.dom.Document;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Set;

/**
 * XML configuration file parser.
 *
 * This class should create properties objects in the same orded as they appear in XML.
 * Usually the first property is more superior than the second and the second one
 * takes over the third one etc.
 */
/**
 * XML配置文件解析
 * 用户的信息会被写入到一个配置文件中
 * 这个类就是用来解析这个这个配置文件的
 * */
public interface ConfigurationParser {

    public void setFileAdapter(FileAdapter fileAdapter);

    /** Initialization
     * @param doc Configuration XML
     */
    /**
     * 初始化XML文件
     * @param doc 使用Document对象初始化*/
    public void initialize(Document doc) throws ParsingException;

    /** Initialization
     * @param filename Path to configuration XML
     */
    /**
     * 是否配置文件初始化解析器
     * @param filename XML的配置文件
     * */
    public void initialize(String filename) throws ParsingException, FileNotFoundException;

    /** Initialization
     * @param in Stream with configuration XML
     */
    /**初始化
     * 配置文件的输入流
     * */
    public void initialize(InputStream in) throws ParsingException;

    /** Create list of users from XML
     * @return List of users and their related objects
     */
    /**
     * 从xml中创建用户列表
     * @return 用户列表
     * */
    public Set<User> createUsers() throws ParsingException;

    /** Get absolute path to users directory
     * @return Absolute name of users directory
     */
    /**
     * 获得用户的绝对路径
     * @return 用户的绝对路径名
     * */
    public String getUsersPath() throws ParsingException;
}
