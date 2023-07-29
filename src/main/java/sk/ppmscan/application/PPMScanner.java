/**
 * 
 */
package sk.ppmscan.application;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.ppmscan.application.beans.Manager;
import sk.ppmscan.application.beans.ScanRun;
import sk.ppmscan.application.beans.Sport;
import sk.ppmscan.application.config.PPMScanConfiguration;
import sk.ppmscan.application.filter.ManagerTeamFilterUtility;
import sk.ppmscan.application.importexport.IgnoredManagersDao;
import sk.ppmscan.application.importexport.ScanRunExporter;
import sk.ppmscan.application.processor.FetchManagerTask;
import sk.ppmscan.application.processor.ProcessedManager;
import sk.ppmscan.application.util.PPMScannerThreadPoolExecutor;

/**
 * 
 * @author miroslavsajko
 *
 */
public class PPMScanner {

	private static final Logger LOGGER = LoggerFactory.getLogger(PPMScanner.class);

	private LocalDateTime appStartTime;

	public PPMScanner() {
		this.appStartTime = LocalDateTime.now();
	}

	public void scan(PPMScanConfiguration configuration, ScanRunExporter scanRunExporter,
			IgnoredManagersDao ignoredManagersDao) throws Exception {

		List<Long> managerIds = getManagerIds(configuration, ignoredManagersDao);

		List<Manager> filteredManagers = Collections.synchronizedList(new LinkedList<>());

		Map<Sport, Integer> filteredTeamsCount = Collections.synchronizedMap(new HashMap<Sport, Integer>());
		configuration.getTeamFilters().keySet().forEach(sport -> filteredTeamsCount.put(sport, 0));

		int sizeOfThreadPool = configuration.getSizeOfThreadPool();
		LOGGER.info("{} threads will be used", sizeOfThreadPool);
		long timeStampBefore = Calendar.getInstance().getTimeInMillis();
		ExecutorService executorService = new PPMScannerThreadPoolExecutor(sizeOfThreadPool, managerIds.size());

		ManagerTeamFilterUtility filterUtility = new ManagerTeamFilterUtility(configuration, appStartTime);

		ScanRun scanRun = new ScanRun();
		scanRun.setScanTime(appStartTime);
		scanRun.setManagers(filteredManagers);

		List<Callable<Entry<Long, ProcessedManager>>> tasks = new LinkedList<Callable<Entry<Long, ProcessedManager>>>();

		for (int i = 0; i < managerIds.size();) {

			for (int j = i; j < i + configuration.getChunkSize() && j < managerIds.size(); j++) {
				Long managerId = managerIds.get(j);

				tasks.add(new FetchManagerTask(configuration.getMillisecondsBetweenPageLoads(), filterUtility,
						configuration.getTeamFilters().keySet(), managerId));
			}
			Set<Long> newIgnoredManagers = Collections.synchronizedSortedSet(new TreeSet<Long>());

			List<Future<Entry<Long, ProcessedManager>>> invokedFutures = executorService.invokeAll(tasks);
			for (Future<Entry<Long, ProcessedManager>> taskResult : invokedFutures) {
				Entry<Long, ProcessedManager> entry = taskResult.get();
				Long managerId = entry.getKey();
				ProcessedManager processedManager = entry.getValue();
				if (processedManager != null) {
					if (processedManager.isIgnorable()) {
						newIgnoredManagers.add(managerId);
					} else if (processedManager.isFilterable()) {
						filteredManagers.add(processedManager.getManager());
						processedManager.getManager().getTeams().forEach(team -> filteredTeamsCount.put(team.getSport(),
								filteredTeamsCount.get(team.getSport()) + 1));
					}
				}
			}

			tasks.clear();

			LOGGER.info("Added {} new managers to the ignore list", newIgnoredManagers.size());

			ignoredManagersDao.exportData(newIgnoredManagers);

			LOGGER.info("Found {} managers in this run", filteredManagers.size());
			for (Entry<Sport, Integer> filteredTeamEntry : filteredTeamsCount.entrySet()) {
				LOGGER.info("Found {} teams in {}", filteredTeamEntry.getValue(), filteredTeamEntry.getKey());
			}

			scanRunExporter.exportData(scanRun);

			i += configuration.getChunkSize();
		}

		long timeStampAfter = Calendar.getInstance().getTimeInMillis();
		Duration duration = new Duration(timeStampAfter - timeStampBefore);

		PeriodFormatter formatter = new PeriodFormatterBuilder().appendHours().appendSuffix("h ").appendMinutes()
				.appendSuffix("m ").appendSeconds().appendSuffix("s").toFormatter();
		String formatted = formatter.print(duration.toPeriod());

		LOGGER.info("The scanning took: {}", formatted);

		executorService.awaitTermination(5, TimeUnit.SECONDS);
		LOGGER.info("{} tasks never finished because of termination", executorService.shutdownNow().size());

	}

	private List<Long> getManagerIds(PPMScanConfiguration configuration, IgnoredManagersDao ignoredManagersImportExport)
			throws Exception {
		List<Long> managerIds = configuration.getManagerIds().stream().collect(Collectors.toList());

		for (long managerId = configuration.getManagerIdFrom(); managerId <= configuration
				.getManagerIdTo(); managerId++) {
			managerIds.add(managerId);
		}
		LOGGER.info("{} managers should be scanned", managerIds.size());

		Set<Long> ignoredManagers = ignoredManagersImportExport.importData();

		LOGGER.info("Applying ignore list of {} managers", ignoredManagers.size());

		managerIds.removeAll(ignoredManagers);
		managerIds.sort((a, b) -> a.compareTo(b));

		LOGGER.info("After applying ignore list: {} managers will be scanned", managerIds.size());

		return managerIds;
	}

}
