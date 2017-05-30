package org.raghnet;


import java.util.Locale;
import java.util.Objects;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;


import org.graylog2.syslog4j.Syslog;
import org.graylog2.syslog4j.SyslogConstants;
import org.graylog2.syslog4j.SyslogIF;
import org.graylog2.syslog4j.impl.unix.socket.UnixSocketSyslogConfig;

public class SyslogHandler extends Handler {


  /**
   * The max size of the message to be sent to syslog.
   */
  private static final int MAX_MESSAGE_LENGTH = 8 * 1024;

  private final String APP_NAME = "TestLog";

  /**
   * The transport protocols supported by this handler.
   */
  private static enum SyslogProtocol {
    UDP(SyslogConstants.UDP), TCP(SyslogConstants.TCP), UNIX_SOCKET(
        SyslogConstants.UNIX_SOCKET), UNIX_SYSLOG(SyslogConstants.UNIX_SYSLOG);

    private final String name;

    SyslogProtocol(String name) {
      this.name = name;
    }
  }

  /**
   * The syslog protocol client used to log messages to the syslog collector.
   */
  private SyslogIF syslog;

  /**
   * Configures a syslog handler from the configuration properties provided by {@link LogManager} or
   * from the default values.
   */
  private void configure() {
    LogManager manager = LogManager.getLogManager();
    String className = getClass().getName();
    String levelName =
        Objects.toString(manager.getProperty(className + ".level"), Level.INFO.getName());
    setLevel(Level.parse(levelName));

    setFormatter(new SimpleFormatter());
    initializeSyslog(manager);
  }

  /**
   * Initializes the syslog for logging.
   * 
   * @param manager global log manager object
   */
  private void initializeSyslog(LogManager manager) {
    System.out.println("Syslog version is " + Syslog.getVersion());
    String className = getClass().getName();
    String protocolName = Objects.toString(manager.getProperty(getClass().getName() + ".protocol"),
        SyslogProtocol.UNIX_SOCKET.toString());
    SyslogProtocol protocol = SyslogProtocol.valueOf(protocolName.toUpperCase(Locale.getDefault()));

    syslog = Syslog.getInstance(protocol.name);
    switch (protocol) {
      case UNIX_SOCKET:

        UnixSocketSyslogConfig syslogConfig = (UnixSocketSyslogConfig) syslog.getConfig();
        String syslogPathName = manager.getProperty(className + ".path");
        System.out.println("Unix_socket path " + syslogPathName);
        if (syslogPathName != null) {
          syslogConfig.setPath(syslogPathName);
        }
        break;

      default:
        int port =
            parseInt(manager.getProperty(className + ".port"), SyslogConstants.SYSLOG_PORT_DEFAULT);
        String host = Objects.toString(manager.getProperty(className + ".host"),
            SyslogConstants.SYSLOG_HOST_DEFAULT);
        syslog.getConfig().setPort(port);
        syslog.getConfig().setHost(host);
        break;
    }
    syslog.getConfig().setFacility(SyslogConstants.FACILITY_LOCAL0);
    syslog.getConfig().setMaxMessageLength(MAX_MESSAGE_LENGTH);
    syslog.getConfig().setIdent(APP_NAME);
    syslog.getConfig().setSendLocalTimestamp(false);
    syslog.getConfig().setIncludeIdentInMessageModifier(true);
    System.out.println("initaializeSyslog called..");
  }

  /**
   * Creates and configures a new syslog logging handler.
   */
  public SyslogHandler() {
    configure();
  }
  
  static  int fromJavaLevel(Level javaLevel)
  {
    
 
      if (javaLevel == Level.OFF) return SyslogConstants.LEVEL_EMERGENCY;
      if (javaLevel ==  Level.SEVERE) return  SyslogConstants.LEVEL_ERROR;
      if (javaLevel ==  Level.WARNING) return  SyslogConstants.LEVEL_WARN;
      if (javaLevel ==  Level.INFO) return  SyslogConstants.LEVEL_INFO;
      if (javaLevel ==  Level.CONFIG) return  SyslogConstants.LEVEL_NOTICE;
      if (javaLevel ==  Level.FINE) return  SyslogConstants.LEVEL_DEBUG;
      if (javaLevel ==  Level.FINER) return  SyslogConstants.LEVEL_DEBUG;
      if (javaLevel ==  Level.FINEST) return  SyslogConstants.LEVEL_DEBUG;
      
      return SyslogConstants.LEVEL_INFO;
 
  }

  /**
   * Converts string into an integer. If the string is <code>null</code>, it returns the given
   * default value.
   * 
   * @param intAsString the string which holds the integer value
   * @param defaultValue the default value to return if the string is null
   * @throws IllegalArgumentException if the string cannot be converted into an integer
   * @return the converted integer value
   */
  private static int parseInt(String intAsString, int defaultValue) {
    if (intAsString == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(intAsString);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public void publish(LogRecord record) {
    if (record == null || !isLoggable(record)) {
      return;
    }
    int syslogLevel = fromJavaLevel(record.getLevel());
    syslog.log(syslogLevel, getFormatter().format(record));
  }

  @Override
  public synchronized void flush() {
    if (syslog != null) {
      syslog.flush();
    }
  }

  @Override
  public synchronized void close() throws SecurityException {
    if (syslog != null) {
      syslog.shutdown();
      syslog = null;
    }
  }
}
