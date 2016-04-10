package com.coldcore.coloradoftp.plugin.webcam.task;

/**
 * This action executes when user uploads file.
 */
public interface UploadTask {

  /** Execute action
   * @param filename Filename (not absolute)
   * @param data File data
   */
  public void execute(String filename, byte[] data);
}
