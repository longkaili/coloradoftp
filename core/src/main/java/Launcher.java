import com.coldcore.coloradoftp.core.Core;
import com.coldcore.coloradoftp.factory.ObjectFactory;
import com.coldcore.coloradoftp.factory.ObjectName;
import com.coldcore.coloradoftp.factory.impl.SpringFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;

/**
 * Launcher.
 *
 * A simple class to start an FTP server as a standalone application.
 * When started, FTP server can be killed by killing the process it is running in, but doing
 * so will terminate the server immediately without calling its stop or poisoned routines.
 *
 *
 * ColoradoFTP - The Open Source FTP Server (http://cftp.coldcore.com)
 */
public class Launcher {

  private static String filename = "core/src/main/resources/beans.xml";


  public static void main(String[] args) {
    System.out.println();
    System.out.println("========================================");
    System.out.println("ColoradoFTP - the open source FTP server");
    System.out.println("Make  sure  to  visit   www.coldcore.com");
    System.out.println("========================================");

    File file = new File(filename);
    if (args.length > 0) file = new File(args[0]);

    System.out.println("Reading configuration from: "+file.getAbsolutePath());
    System.out.println("To set a different configuration file use 'Launcher filename'");

    if (!file.exists()) {
      System.out.println("Configuration file not found, terminating...");
      System.exit(1);
    }

    try {
      Resource resource = new FileSystemResource(file);
      ObjectFactory.setInternalFactory(new SpringFactory(resource));
    } catch (Throwable e) {
      System.out.println("Cannot initialize object factory, terminating...");
      e.printStackTrace();
      System.exit(1);
    } 
    System.out.println("Object factory initialized");

    Core core = null;
    try {
      core = (Core) ObjectFactory.getObject(ObjectName.CORE);
      core.start();
    } catch (Throwable e) {
      System.out.println("Unable to start the server, terminating...");
      e.printStackTrace();
      System.exit(1);
    }

    //todo Shutdown Hook
    //addShutdownHook(core);

    System.out.println("Server started, use Ctrl+C to kill the process");
  }

/*
  private static void addShutdownHook(final Core core) {
    Runnable shutdownHook = new Runnable() {
      public void run() {
        System.out.println("Stopping server...");
        core.poison();
        //todo wait till everyone disconnects
        core.stop();
      }
    };
    Runtime runtime = Runtime.getRuntime();
    runtime.addShutdownHook(new Thread(shutdownHook));
  }
*/

}
