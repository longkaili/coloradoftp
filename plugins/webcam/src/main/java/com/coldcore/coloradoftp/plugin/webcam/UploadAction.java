package com.coldcore.coloradoftp.plugin.webcam;

import com.coldcore.coloradoftp.plugin.webcam.task.UploadTask;
import com.coldcore.misc5.CByte;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Executes action when file is ready.
 */
public class UploadAction implements Runnable {

  private static Logger log = Logger.getLogger(UploadAction.class);
  protected String filename;
  protected FileGrabber grabber;
  protected UploadTask task;


  public UploadAction(String filename, FileGrabber grabber, UploadTask task) {
    this.filename = filename;
    this.grabber = grabber;
    this.task = task;
  }


  public void run() {
    byte[] data = readFile();
    if (data != null) task.execute(filename, data);
  }


  /** Read file data.
   * @return File data or NULL in case of error
   */
  protected byte[] readFile() {
    try {
      InputStream in = grabber.getInputStream();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      CByte.inputToOutput(in, out);
      return out.toByteArray();
    } catch (Exception e) {
      log.error("Error reading file (stopping)", e);
      return null;
    }
  }
}
