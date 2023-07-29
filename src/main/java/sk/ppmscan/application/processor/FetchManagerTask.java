package sk.ppmscan.application.processor;

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.WebClient;

import sk.ppmscan.application.beans.Manager;
import sk.ppmscan.application.beans.Sport;
import sk.ppmscan.application.beans.Team;
import sk.ppmscan.application.filter.ManagerTeamFilterUtility;
import sk.ppmscan.application.pageparser.ManagerReader;
import sk.ppmscan.application.pageparser.TeamReader;
import sk.ppmscan.application.util.WebClientUtil;

public class FetchManagerTask implements Callable<Entry<Long, ProcessedManager>> {

	private static final String MANAGER_PROFILE_URL = "https://ppm.powerplaymanager.com/en/manager-profile.html?data=";

	private static final Logger LOGGER = LoggerFactory.getLogger(FetchManagerTask.class);

	private long managerId;

	private long millisecondsBetweenPageLoads;

	private ManagerTeamFilterUtility filterUtility;
	
	private Set<Sport> sportsToLoad;

	public FetchManagerTask(long millisecondsBetweenPageLoads, ManagerTeamFilterUtility filterUtility, Set<Sport> sportsToLoad,
			long managerId) {
		this.millisecondsBetweenPageLoads = millisecondsBetweenPageLoads;
		this.filterUtility = filterUtility;
		this.managerId = managerId;
		this.sportsToLoad = sportsToLoad;
	}

	@Override
	public Entry<Long, ProcessedManager> call() {
		try (WebClient client = WebClientUtil.createWebClient()) {
			Thread.sleep(getWaitTime());
			Manager manager = ManagerReader.readManagerInfo(client.getPage(MANAGER_PROFILE_URL + managerId));

			ProcessedManager processedManager = new ProcessedManager(manager);

			if (filterUtility.isIgnorable(manager)) {
				processedManager.setIgnorable(true);
			} else if (filterUtility.fitsFilterCriteria(manager)) {
				processedManager.setFilterable(true);
				for (Team team : manager.getTeams()) {
					if (sportsToLoad.contains(team.getSport())) {
						Thread.sleep(getWaitTime());
						TeamReader.readTeamInfo(team, client.getPage(team.getUrl()));
					}
				}
				manager.setTeams(manager.getTeams().stream().filter(filterUtility::fitsFilterCriteria)
						.collect(Collectors.toList()));
			}
			return new AbstractMap.SimpleEntry<Long, ProcessedManager>(managerId, processedManager);
		} catch (Exception e) {
			LOGGER.error("Error during loading of manager  " + managerId, e);
			return new AbstractMap.SimpleEntry<Long, ProcessedManager>(managerId, null);
		}
	}
	
	private long getWaitTime() {
		return this.millisecondsBetweenPageLoads + new Random().nextInt(200);
	}

}
