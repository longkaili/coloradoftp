package com.coldcore.coloradoftp.plugin.webcam.task;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Submits file to HTTP server.
 */
public class HttpSubmitTask implements UploadTask, Runnable {

  private static Logger log = Logger.getLogger(HttpSubmitTask.class);
  protected String submitUrl;
  protected String fileParameterName;
  protected Map<String,String> parameters;
  protected List<Holder> queue;
  protected Thread thr;
  protected boolean running;
  protected int queueSize;


  public class Holder {
    public String filename;
    public byte[] data;
  }


  public HttpSubmitTask() {
    queue = new ArrayList<Holder>();
    queueSize = 10;
  }


  /** Get URL where to submit data
   * @return URL
   */
  public String getSubmitUrl() {
    return submitUrl;
  }


  /** Set URL where to submit data
   * @param submitUrl URL
   */
  public void setSubmitUrl(String submitUrl) {
    this.submitUrl = submitUrl;
  }


  /** Get name of file parameter
   * @return File parameter name
   */
  public String getFileParameterName() {
    return fileParameterName;
  }


  /** Set name of file parameter
   * @param fileParameterName File parameter name
   */
  public void setFileParameterName(String fileParameterName) {
    this.fileParameterName = fileParameterName;
  }


  /** Get map with user-defined parameters
   * @return Parameters map
   */
  public Map<String, String> getParameters() {
    return parameters;
  }


  /** Set map with user-defined parameters
   * @param parameters Parameters map
   */
  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }


  /** Get queue size (0 no limit)
   * @return Queue size
   */
  public int getQueueSize() {
    return queueSize;
  }


  /** Set queue size (0 no limit)
   * @param queueSize Queue size
   */
  public void setQueueSize(int queueSize) {
    if (queueSize < 0) throw new IllegalArgumentException("Negative queue size");
    this.queueSize = queueSize;
  }


  synchronized public void execute(String filename, byte[] data) {
    synchronized (queue) {

      Holder holder = new Holder();
      holder.filename = filename;
      holder.data = data;
      queue.add(holder);

      if (queueSize > 0 && queue.size() > queueSize) {
        log.debug("Queue overflow");
        queue.remove(0);
      }

    }

    //Start the thread to submit files in the queue
    if (!running) {
      running = true;
      thr = new Thread(this);
      thr.start();
    }
  }


  public void run() {
    while (running)
      try {

        Holder holder;
        synchronized (queue) {
          if (queue.isEmpty()) {
            log.debug("Queue is empty");
            running = false;
            return;
          }

          holder = queue.remove(0);
        }

        submit(holder.filename, holder.data);
        log.debug("Submitted '"+holder.filename+"' "+holder.data.length+" bytes");

      } catch (Exception e) {
        log.error("Error while trying to submit data (ignoring)", e);
      }
  }


  /** Submit file via HTTP
   * @param filename File name
   * @param data File data
   */
  protected void submit(String filename, byte[] data) throws Exception {
    if (filename == null) throw new IllegalStateException("File name is missing");
    if (data == null) throw new IllegalStateException("File data is missing");
    if (submitUrl == null) throw new IllegalStateException("Submit URL not set");
    if (fileParameterName == null) throw new IllegalStateException("File parameter name not set");

    List<Part> parts = new ArrayList<Part>();
    Part filePart = new FilePart(fileParameterName, new ByteArrayPartSource(filename, data));
    parts.add(filePart);

    if (parameters != null)
      for (String key : parameters.keySet()) {
        Part part = new StringPart(key, parameters.get(key));
        parts.add(part);
      }

    HttpClient client = new HttpClient();
    PostMethod method = new PostMethod(submitUrl);

    Part[] p = parts.toArray(new Part[parts.size()]);
    method.setRequestEntity(new MultipartRequestEntity(p, method.getParams()));

    client.executeMethod(method);
    method.releaseConnection();
  }
}
