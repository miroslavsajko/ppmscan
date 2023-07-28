package sk.ppmscan.application.util;

import java.text.DecimalFormat;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.ppmscan.application.PPMScanner;

public class PPMScannerThreadPoolExecutor extends ThreadPoolExecutor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PPMScanner.class);
	
	private DecimalFormat FORMAT = new DecimalFormat("0.0");
	
	private long expectedTaskCount;

	public PPMScannerThreadPoolExecutor(int numberOfThreads, long expectedTaskCount) {
		super(numberOfThreads, numberOfThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		this.expectedTaskCount = expectedTaskCount;
	}

	@Override
	protected void afterExecute(Runnable runnable, Throwable exception) {
		super.afterExecute(runnable, exception);

		if (exception != null) {
			this.submit(runnable);
		} else {
			long completedCount = this.getCompletedTaskCount();
			if (completedCount > 1 && completedCount % 10 == 0) {
				double totalCount = this.expectedTaskCount;
				double percentage = completedCount / totalCount;
				LOGGER.info("{} ({}%) managers successfully loaded", completedCount, FORMAT.format(percentage * 100)); 
			}
		}
	}
	
}
