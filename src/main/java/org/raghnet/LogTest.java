package org.raghnet;

import java.util.logging.Logger;

public class LogTest {
  
  private static final Logger LOGGER = Logger.getLogger( LogTest.class.getName() );
  
  public static void main(String[] args)
  {
    
    LOGGER.info("Hello world");
  }

}
