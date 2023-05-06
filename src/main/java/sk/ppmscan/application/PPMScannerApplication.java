/**
 * 
 */
package sk.ppmscan.application;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
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
import sk.ppmscan.application.beans.Sport;
import sk.ppmscan.application.beans.Team;
import sk.ppmscan.application.config.Configuration;
import sk.ppmscan.application.importexport.FilteredTeamsExporter;
import sk.ppmscan.application.importexport.IgnoredManagersImportExport;
import sk.ppmscan.application.processor.FetchReadProcessManagerPageTask;
import sk.ppmscan.application.processor.ProcessedManager;
import sk.ppmscan.application.util.PPMScannerThreadPoolExecutor;

/**
 * 
 * @author miroslavsajko
 *
 */
public class PPMScannerApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(PPMScannerApplication.class);

	private LocalDateTime appStartTime;

	public PPMScannerApplication() {
		this.appStartTime = LocalDateTime.now();
	}

	public void scan(Configuration configuration) throws Exception {
		
		IgnoredManagersImportExport ignoredManagersImportExport = configuration.getIgnoredManagersFormat().getIgnoredManagersImportExporter();
		
		List<Long> managerIds = getManagerIds(configuration, ignoredManagersImportExport);

		Set<Manager> filteredManagers = Collections.synchronizedSet(new HashSet<>());
		Map<Sport, Set<Team>> filteredTeams = Collections.synchronizedMap(new TreeMap<Sport, Set<Team>>());
		configuration.getTeamFilters().keySet()
				.forEach(sport -> filteredTeams.put(sport, Collections.synchronizedSet(new TreeSet<>())));

		int sizeOfThreadPool = configuration.getSizeOfThreadPool();
		LOGGER.info("{} threads will be used", sizeOfThreadPool);
		long timeStampBefore = Calendar.getInstance().getTimeInMillis();
		ExecutorService executorService = new PPMScannerThreadPoolExecutor(sizeOfThreadPool, managerIds.size());

		FilteredTeamsExporter filteredTeamsExporter = configuration.getExportFormat().getFilteredTeamsExporter(this.appStartTime);

		List<Callable<Entry<Long, ProcessedManager>>> tasks = new LinkedList<Callable<Entry<Long, ProcessedManager>>>();

		for (int i = 0; i < managerIds.size();) {

			for (int j = i; j < i + configuration.getChunkSize() && j < managerIds.size(); j++) {
				Long managerId = managerIds.get(j);
				tasks.add(new FetchReadProcessManagerPageTask(configuration, managerId, appStartTime));
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
						for (Team team : processedManager.getFilterableTeams()) {
							filteredTeams.get(team.getSport()).add(team);
						}
					}
				}
			}

			tasks.clear();

			LOGGER.info("Added {} new managers to the ignore list", newIgnoredManagers.size());

			ignoredManagersImportExport.exportData(newIgnoredManagers);

			LOGGER.info("Found {} managers in this run", filteredManagers.size());
			for (Entry<Sport, Set<Team>> filteredTeamEntry : filteredTeams.entrySet()) {
				LOGGER.info("Found {} teams in {}", filteredTeamEntry.getValue().size(), filteredTeamEntry.getKey());
			}

			filteredTeamsExporter.exportData(filteredTeams);

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
	
	private List<Long> getManagerIds(Configuration configuration, IgnoredManagersImportExport ignoredManagersImportExport) throws Exception {
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
