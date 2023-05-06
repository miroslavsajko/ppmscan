package sk.ppmscan.application.processor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import sk.ppmscan.application.beans.Manager;
import sk.ppmscan.application.beans.Team;
import sk.ppmscan.application.config.Configuration;
import sk.ppmscan.application.config.TeamFilterConfiguration;
import sk.ppmscan.application.pageparser.ManagerReader;
import sk.ppmscan.application.pageparser.TeamReader;
import sk.ppmscan.application.util.WebClientUtil;

public class FetchReadProcessManagerPageTask implements Callable<Entry<Long, ProcessedManager>> {

	private static final String MANAGER_PROFILE_URL = "https://ppm.powerplaymanager.com/en/manager-profile.html?data=";

	private static final Logger LOGGER = LoggerFactory.getLogger(FetchReadProcessManagerPageTask.class);

	private long managerId;

	private Configuration configuration;

	private LocalDateTime appStartTime;

	public FetchReadProcessManagerPageTask(Configuration configuration, long managerId, LocalDateTime appStartTime) {
		this.configuration = configuration;
		this.managerId = managerId;
		this.appStartTime = appStartTime;
	}

	@Override
	public Entry<Long, ProcessedManager> call() {
		try {
			WebClient client = WebClientUtil.createWebClient();
			HtmlPage managerPage;
			managerPage = client.getPage(MANAGER_PROFILE_URL + managerId);
			Thread.sleep(configuration.getMillisecondsBetweenPageLoads());
			Manager manager = ManagerReader.readManagerInfo(managerPage);
			ProcessedManager processedManager = new ProcessedManager(manager);
			if (isIgnorable(manager)) {
				processedManager.setIgnorable(true);
			} else if (applyManagerFilters(manager)) {
				processedManager.setFilterable(true);
				processedManager.setFilterableTeams(new HashSet<>());
				for (Team team : manager.getTeams()) {
					TeamReader.readTeamInfo(team, client.getPage(team.getUrl()));
					if (applyTeamFilters(team)) {
						processedManager.getFilterableTeams().add(team);
					}
					Thread.sleep(configuration.getMillisecondsBetweenPageLoads());
				}
			}
			client.close();
			return new AbstractMap.SimpleEntry<Long, ProcessedManager>(managerId, processedManager);
		} catch (Exception e) {
			LOGGER.error("Error during loading of manager  " + managerId, e);
			return new AbstractMap.SimpleEntry<Long, ProcessedManager>(managerId, null);
		}
	}

	/**
	 * Manager is ignorable when he is blocked or when he has no recent logins.
	 * 
	 * @param manager manager
	 * @return true if this manager should be ignored
	 */
	private boolean isIgnorable(Manager manager) {
		if (manager.isBlocked()) {
			LOGGER.info("Manager {} is blocked", manager.getId());
			return true;
		}
		if (CollectionUtils.isEmpty(manager.getRecentLogins())) {
			LOGGER.info("Manager {} doesnt have any recent logins", manager.getId());
			return true;
		}
		long lastLoginMonthsAgo = ChronoUnit.MONTHS.between(manager.getRecentLogins().get(0), this.appStartTime);
		if (lastLoginMonthsAgo > this.configuration.getIgnoreListLastLoginMonthsThreshold()) {
			LOGGER.info("Manager {} logged in the last time {} months ago", manager.getId(), lastLoginMonthsAgo);
			return true;
		}
		return false;
	}

	private boolean applyManagerFilters(Manager manager) {
		if (CollectionUtils.isEmpty(manager.getTeams())) {
			LOGGER.debug("Manager {} has no teams", manager.getId());
			return false;
		}

		int recentLoginFilterCriteriaMatchCount = 0;

		LocalDateTime mostRecentLogin = manager.getRecentLogins().get(0);

		long mostRecentLoginDaysAgo = ChronoUnit.DAYS.between(mostRecentLogin, this.appStartTime);
		if (mostRecentLoginDaysAgo < configuration.getLastLoginDaysRecentlyActiveThreshold()) {
			LOGGER.debug("Manager {} was active just recently", manager.getId());
		} else {
			recentLoginFilterCriteriaMatchCount++;
		}

		long dayDifferenceBuffer = 0;
		for (int i = 0; i < manager.getRecentLogins().size() - 1; i++) {
			long loginDayDifference = ChronoUnit.DAYS.between(manager.getRecentLogins().get(i), this.appStartTime);
			dayDifferenceBuffer += loginDayDifference;
		}

		if (dayDifferenceBuffer < configuration.getLastLoginDayDifferenceSumThreshold()) {
			// the user is probably regularly active
			LOGGER.debug("Manager {} is probably regularly active, dayDifferenceBuffer: {}", manager.getId(),
					dayDifferenceBuffer);
		} else {
			recentLoginFilterCriteriaMatchCount++;
		}

		if (recentLoginFilterCriteriaMatchCount < this.configuration.getLastLoginCriteriaMatch()) {
			LOGGER.debug("Manager {} does not match enough criteria ({}/{})", manager.getId(),
					recentLoginFilterCriteriaMatchCount, this.configuration.getLastLoginCriteriaMatch());
			return false;
		}

		LOGGER.info("Manager {} with {} teams fits the manager criteria", manager.getId(), manager.getTeams().size());
		return true;
	}

	private boolean applyTeamFilters(Team team) {
		TeamFilterConfiguration teamFilterConfiguration = this.configuration.getTeamFilters().get(team.getSport());
		if (teamFilterConfiguration == null) {
			LOGGER.debug("Manager {}'s team in {} is not in configuration - team ignored", team.getManager().getId(),
					team.getSport());
			return false;
		}
		for (Entry<String, Long> minStrengthConfigEntry : teamFilterConfiguration.getMinTeamStrengths().entrySet()) {
			String teamStrengthAttribute = minStrengthConfigEntry.getKey();
			Long teamStrength = team.getTeamStrength().get(teamStrengthAttribute);
			if (teamStrength == null || teamStrength < minStrengthConfigEntry.getValue()) {
				LOGGER.debug("Manager {}'s team in {} does not match minimum strength criteria ({} {} < {})",
						team.getManager().getId(), team.getSport(), teamStrengthAttribute, teamStrength,
						minStrengthConfigEntry.getValue());
				return false;
			}
		}
		LOGGER.debug("Manager {}'s team in {} fits the criteria", team.getManager().getId(), team.getSport());
		return true;
	}

}
