package com.coldcore.coloradoftp.connection.impl;

import com.coldcore.coloradoftp.connection.DataPortListener;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Factory-like class to create a sequence(序列) of data port listeners.
 * This class can be used as an imput parameter(参数) into data port listener set.
 * <p/>
 * Class contructor takes port numbers and creates a sequence of data port listeners with assigned
 * port from port 1 to port 2 (inclusive). A very easy way rather than to create every data port
 * listener individualy.
 * <p/>
 * This class relies on the ObjectFactory to get instances of DataPortListeners, but the ObjectFactory
 * cannot be used in a constructor. This class overwrites important methods of the HashSet initializing
 * itself when those will be called (e.g. by DataPortListenerSet).
 */

/**数据监听序列，一次建立一系列端口的数据监听对象，并将它们加入一个集合之中
 　　　这样可以提升使用效率
 */

public class DataPortListenerSequence extends HashSet<DataPortListener> {

    //对应的端口port1--port2
    protected int     port1;
    protected int     port2;
    protected boolean initialized; //记录是否已经初始化


    //构造器，指定两个初始化的端口
    public DataPortListenerSequence(int port1, int port2) {
        this.port1 = port1;
        this.port2 = port2;
    }


    //初始化函数，使用ObjectFactory生成port1--port2之间的数据端口监听对象，并将他们加入集合之中
    protected void initialize() {
        if (initialized) {
            return;
        }//如果已经初始化了就直接返回
        initialized = true;
        for (int port = port1; port <= port2; port++) {
            DataPortListener listener = (DataPortListener) ObjectFactory.getObject(ObjectName.DATA_PORT_LISTENER);
            listener.setPort(port);
            add(listener);
        }
    }


    //返回集合对应的迭代器

    public Iterator<DataPortListener> iterator() {
        initialize();//如果还没有初始化，则先初始化
        return super.iterator();
    }


    //返回集合的大小
    public int size() {
        initialize();
        return super.size();
    }


    //返回集合是否为空
    public boolean isEmpty() {
        initialize();
        return super.isEmpty();
    }
}
