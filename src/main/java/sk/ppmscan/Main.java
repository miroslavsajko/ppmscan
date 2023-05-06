package sk.ppmscan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.ppmscan.application.PPMScannerApplication;
import sk.ppmscan.application.config.Configuration;
import sk.ppmscan.application.config.ConfigurationHolder;

/**
 * Main Runner.
 *
 */
public class Main {

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {

		LOGGER.info("PPMScan start");
		try {
			ConfigurationHolder configurationHolder = ConfigurationHolder.getInstance();
			Configuration configuration = configurationHolder.getConfiguration();
			PPMScannerApplication application = new PPMScannerApplication();
			application.scan(configuration);
		} catch (Exception e) {
			LOGGER.error("Error: {}", e);
		}
		
		LOGGER.info("PPMScan end");

	}

}
