package com.coldcore.coloradoftp.plugin.webcam;

import com.coldcore.coloradoftp.filter.impl.GenericDataFilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

/**
 * This filter grabs uploaded file and provides an input stream.
 */
public class FileGrabber extends GenericDataFilter {

  protected PipedInputStream pin;
  protected WritableByteChannel twbc;


  public FileGrabber() throws IOException {
    PipedOutputStream pout = new PipedOutputStream();
    twbc = Channels.newChannel(pout);
    pin = new PipedInputStream(pout);
  }


  public int write(ByteBuffer src) throws IOException {
    return twbc.write(src);
  }


  /** Get input stream */
  public InputStream getInputStream() {
    return pin;
  }


  public void close() throws IOException {
    twbc.close();
  }
}
