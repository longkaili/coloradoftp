package com.coldcore.coloradoftp.plugin.webcam;

import com.coldcore.coloradoftp.filesystem.FailedActionException;
import com.coldcore.coloradoftp.filesystem.FailedActionReason;
import com.coldcore.coloradoftp.filesystem.FileSystem;
import com.coldcore.coloradoftp.plugin.webcam.task.UploadTask;
import com.coldcore.coloradoftp.session.Session;
import com.coldcore.coloradoftp.session.SessionAttributeName;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.HashSet;
import java.util.Set;

/**
 * Wraps existing file system and executes plugins for selected users.
 */
public class FileSystemWrapper extends com.coldcore.coloradoftp.filesystem.impl.FileSystemWrapper {

  private static Logger log = Logger.getLogger(FileSystemWrapper.class);
  protected Set<UploadInterceptor> interceptors;


  public FileSystemWrapper(FileSystem fileSystem) {
    super(fileSystem);

    interceptors = new HashSet<UploadInterceptor>();
  }


  /** Get interceptors
   * @return Interceptors
   */
  public Set<UploadInterceptor> getInterceptors() {
    return interceptors;
  }


  /** Set interceptors
   * @param interceptors Interceptors
   */
  public void setInterceptors(Set<UploadInterceptor> interceptors) {
    this.interceptors = interceptors;
  }


  public WritableByteChannel saveFile(String filename, boolean append, Session userSession) throws FailedActionException {
    //Test if this operation needs to be intercepted
    String username = (String) userSession.getAttribute(SessionAttributeName.USERNAME);
    UploadInterceptor interceptor = null;
    for (UploadInterceptor i : interceptors)
      if (username.equalsIgnoreCase(i.getUsername())) {
        interceptor = i;
        break;
      }

    if (interceptor != null) {
      log.debug("Upload intercepted, user: "+interceptor.getUsername());
      if (append) throw new FailedActionException(FailedActionReason.NOT_IMPLEMENTED, "Append is not allowed");

      UploadTask task = interceptor.getTask();
      if (task == null) {
        log.error("No action assigned to interceptor");
        throw new FailedActionException(FailedActionReason.SYSTEM_ERROR);
      }

      //This object will read the file into memory
      FileGrabber grabber;
      try {
        grabber = new FileGrabber();
      } catch (IOException e) {
        log.error("Cannot construct file grabber", e);
        throw new FailedActionException(FailedActionReason.SYSTEM_ERROR);
      }

      String lname = filename.indexOf(getFileSeparator()) == -1 ? filename :
              filename.substring(filename.lastIndexOf(getFileSeparator())+getFileSeparator().length());

      //Run thread to read the file and execute all interceptor tasks
      UploadAction action = new UploadAction(lname, grabber, task);
      Thread thr = new Thread(action);
      thr.start();

      return grabber;
    }

    //Continue as normal
    return fileSystem.saveFile(filename, append, userSession);
  }

}
