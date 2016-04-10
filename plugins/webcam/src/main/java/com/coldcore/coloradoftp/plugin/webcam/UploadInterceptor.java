package com.coldcore.coloradoftp.plugin.webcam;

import com.coldcore.coloradoftp.plugin.webcam.task.UploadTask;

/**
 * Intercepts uploads that suppose to execute special web cam action instead of common
 * filesystem save routines.
 */
public class UploadInterceptor {

  protected String username;
  protected UploadTask task;


  /** Get username
   * @return Username
   */
  public String getUsername() {
    return username;
  }


  /** Set username
   * @param username Username
   */
  public void setUsername(String username) {
    this.username = username;
  }


  /** Get action to execute upon upload
   * @return Task
   */
  public UploadTask getTask() {
    return task;
  }


  /** Set action to execute upon upload
   * @param task Task
   */
  public void setTask(UploadTask task) {
    this.task = task;
  }
}
