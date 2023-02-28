/** Copyright 2022 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.taq.log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Properties;

import org.xmlpull.v1.XmlPullParserException;

import com.j256.simplelogging.LogBackendType;
import com.j256.simplelogging.Logger;
import com.j256.simplelogging.LoggerFactory;

import au.com.cybersearch2.taq.Singleton;

/**
 * Configures {@link java.util.logging.LogManager}
 */
public class LogManager {

	private static final String PROPERTIES_PATH = "/logging.xml";

    private final LogBackendType logBackendType;
    
    private boolean isInitialized;
	
	/**
	 * Default constructor analyzes environment to determine which backend to use
	 */
	public LogManager() {
		logBackendType = findLogBackendType();
	}

	/**
	 * Perform one-time initialization. A strategy to configure logging is employed if
	 * the Java Util logging backend is selected.
	 */
	public static void initialize() {
		getSingleton().initialzeLogging();
	}

	/** 
	 * Creates and returns logger for given class 
	 * @param clazz Class to log
	 */
	public static Logger getLogger(Class<?> clazz) {
		return getSingleton().createLogBackend(clazz);
	}

	private void initialzeLogging() {
		if (isInitialized)
			return;
		if (logBackendType == LogBackendType.JAVA_UTIL)
			initializeJavaLogging();
		else
		    LoggerFactory.setLogBackendType(logBackendType);
		isInitialized = true;
	}

	private static LogManager getSingleton() {
		return (LogManager)Singleton.log_manager.getObject();
	}
	
	/**
	 * Return a logger associated with a particular class
	 */
	private Logger createLogBackend(Class<?> clazz) {
		if (!isInitialized)
			initialzeLogging();
		return new Logger(logBackendType.createLogBackend(clazz.getName()));
	}
	
    private void initializeJavaLogging() {
		java.util.logging.LogManager javaLogManager = java.util.logging.LogManager.getLogManager();
		String taqLoggingConfigPath = null;
		try {
			taqLoggingConfigPath = getConfigurationPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (taqLoggingConfigPath != null) {
			File loggingFile = new File(taqLoggingConfigPath);
			if (loggingFile.exists()) {
	            String homeFilePath = System.getProperty("user.home");
				try (InputStream is = new FileInputStream(loggingFile)) {
					XmlConfiguration xmlConfiguration = new XmlConfiguration();
					Properties props = xmlConfiguration.parseXmlConfiguration(is);
					String propertiesPath;
		            if (homeFilePath != null)
		            	propertiesPath = Paths.get(homeFilePath, ".taq", "logging.properties")
			                    .toAbsolutePath().normalize().toString();
		            else {
						int pos = taqLoggingConfigPath.lastIndexOf('.');
						if (pos > 0)
							propertiesPath = taqLoggingConfigPath.substring(0, pos + 1) + "properties";
						else
							propertiesPath = taqLoggingConfigPath + ".properties";
		            }
			        try (OutputStream out = new FileOutputStream(propertiesPath)) {
			        	props.store(out, "Generated Logging configuration - DO NOT EDIT");
			        	try (final InputStream in = new FileInputStream(propertiesPath)) {
							javaLogManager.reset();
							javaLogManager.updateConfiguration(new BufferedInputStream(in), null);
			        	}
			        }
				} catch (FileNotFoundException e) {
					// This should not happen as the file is supposed to exist
					System.err.println(String.format("Logging config file %s not found", taqLoggingConfigPath));
				} catch (IOException e) {
					System.err.println(String.format("Error \"%s\"while reading Logging config file %s", e.getMessage(),  taqLoggingConfigPath));
				} catch (XmlPullParserException e) {
					System.err.println(String.format("Error \"%s\"while parsing Logging config file %s", e.getMessage(),  taqLoggingConfigPath));
				}
			}
		}
	}

	private String getConfigurationPath() throws IOException {
        String filePath = System.getProperty("taq.logging.config.file");
        if (filePath == null) {
            filePath = System.getProperty("user.home");
            if (filePath != null)
	            filePath = Paths.get(filePath, ".taq", "logging.xml")
	                    .toAbsolutePath().normalize().toString();
            File loggingConfig = new File(filePath);
            if (!loggingConfig.exists()) {
    			URL url = LogManager.class.getResource(PROPERTIES_PATH);
    			if (url != null)
	         	    try { 
	        	    	filePath = url.toURI().getPath();
	        	    } catch (URISyntaxException e) {
	         	    }
    			 else
    	            filePath = null;
            }
        }
        return filePath;
    }

	/**
	 * Return the most appropriate log backend type. Defaults to Java util logging.
	 */
	private LogBackendType findLogBackendType() {

		// See if the log-type was specified as a system property
		String logTypeString = System.getProperty(LoggerFactory.LOG_TYPE_SYSTEM_PROPERTY);
		if (logTypeString != null) {
			try {
				return LogBackendType.valueOf(logTypeString);
			} catch (IllegalArgumentException e) {
				System.err.println("Could not find valid log-type from system property '"
						+ LoggerFactory.LOG_TYPE_SYSTEM_PROPERTY + "', value '" + logTypeString + "'");
			}
		} else 
			for (LogBackendType logType : LogBackendType.values()) {
				// Commons logging is a dependency of BeanUtils, so must be excluded.
				// use LOG_TYPE_SYSTEM_PROPERTY system property if CommonsLogging is required
				if (logType.isAvailable() && 
					(logType != LogBackendType.COMMONS_LOGGING) &&
					(logType != LogBackendType.LOCAL) &&
					(logType != LogBackendType.CONSOLE)) {
					return logType;
				}
			}
		// Fall back is always JAVA_UTIL
		return LogBackendType.JAVA_UTIL;
	}
}
