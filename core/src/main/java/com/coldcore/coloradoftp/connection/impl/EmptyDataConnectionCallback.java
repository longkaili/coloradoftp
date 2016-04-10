package com.coldcore.coloradoftp.connection.impl;

import com.coldcore.coloradoftp.connection.DataConnectionCallback;
import com.coldcore.coloradoftp.connection.DataConnection;

import org.apache.log4j.Logger;

/**
 * @see com.coldcore.coloradoftp.connection.DataConnectionCallback
 *
 * An empty callback implementation which does nothing.
 * It is recommended to extended this class to stay independent of changes in case if more
 * methods will later be added to DataConnectionCallback interface.
 */
/**这是一个空类，没有具体的实现
是为了以后能给DataConnectionCallback接口提供更多的功能并同时保持独立性做准备做准备*/
public class EmptyDataConnectionCallback implements DataConnectionCallback {

  private static Logger log = Logger.getLogger(EmptyDataConnectionCallback.class);


  public void onTransferComplete(DataConnection source) {
    log.debug("No task to execute onTransferComplete");
  }


  public void onTransferAbort(DataConnection source) {
    log.debug("No task to execute onTransferAbort");
  }
}
