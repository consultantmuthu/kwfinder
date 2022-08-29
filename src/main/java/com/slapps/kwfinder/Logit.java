package com.slapps.kwfinder;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 * 
 * @author Muthukumaran
 *
 */
public class Logit {

	static String fqcn 		= Logit.class.getName();
	static Logger logger 	= null;
	static {
		try {
			logger = Logit.getLogger();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static Logger getLogger() throws IOException {
		
		PatternLayout layout = new PatternLayout();
		String conversionPattern = "%-7p %d [%t] %c %x - %m%n";
		layout.setConversionPattern(conversionPattern);

		// creates console appender
		ConsoleAppender consoleAppender = new ConsoleAppender();
		consoleAppender.setLayout(layout);
		consoleAppender.activateOptions();

		// creates file appender
		FileAppender fileAppender = new RollingFileAppender();
		// Path temp = Files.createTempFile("slapps", ".log");
		fileAppender.setFile(System. getProperty("java.io.tmpdir") + File.separator + "slapps.log");
		fileAppender.setLayout(layout);
		fileAppender.activateOptions();

		// configures the root logger
		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.INFO);
		rootLogger.addAppender(consoleAppender);
		rootLogger.addAppender(fileAppender);

		// creates a custom logger and log messages
		logger = Logger.getLogger(Logit.class.getName());
		Logit.log(fqcn, "Initialized logs....");
		return logger;
	}

	public static void log(String fqcn, String msg) {
		logger.info(fqcn + " " + msg);
	}

}
