package sk.ppmscan.application.filter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.ppmscan.application.beans.Manager;
import sk.ppmscan.application.beans.Team;
import sk.ppmscan.application.config.PPMScanConfiguration;
import sk.ppmscan.application.config.TeamFilterConfiguration;

public class ManagerTeamFilterUtility {

	private static final Logger LOGGER = LoggerFactory.getLogger(ManagerTeamFilterUtility.class);

	private PPMScanConfiguration configuration;

	private LocalDateTime appStartTime;

	public ManagerTeamFilterUtility(PPMScanConfiguration configuration, LocalDateTime appStartTime) {
		this.configuration = configuration;
		this.appStartTime = appStartTime;
	}

	/**
	 * Manager can be ignored when he is blocked or when he has no recent logins.
	 * 
	 * @param manager manager
	 * 
	 * @return true if this manager should be ignored
	 */
	public boolean isIgnorable(Manager manager) {
		if (manager.isBlocked()) {
			LOGGER.info("Manager {} is blocked", manager.getManagerId());
			return true;
		}
		if (CollectionUtils.isEmpty(manager.getRecentLogins())) {
			LOGGER.info("Manager {} doesnt have any recent logins", manager.getManagerId());
			return true;
		}
		long lastLoginMonthsAgo = ChronoUnit.MONTHS.between(manager.getRecentLogins().get(0), this.appStartTime);
		if (lastLoginMonthsAgo > this.configuration.getIgnoreListLastLoginMonthsThreshold()) {
			LOGGER.info("Manager {} logged in the last time {} months ago", manager.getManagerId(), lastLoginMonthsAgo);
			return true;
		}
		return false;
	}

	public boolean fitsFilterCriteria(Manager manager) {
		if (CollectionUtils.isEmpty(manager.getTeams())) {
			LOGGER.debug("Manager {} has no teams", manager.getManagerId());
			return false;
		}

		int recentLoginFilterCriteriaMatchCount = 0;

		LocalDateTime mostRecentLogin = manager.getRecentLogins().get(0);

		long mostRecentLoginDaysAgo = ChronoUnit.DAYS.between(mostRecentLogin, this.appStartTime);
		if (mostRecentLoginDaysAgo < configuration.getLastLoginDaysRecentlyActiveThreshold()) {
			LOGGER.debug("Manager {} was active just recently", manager.getManagerId());
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
			LOGGER.debug("Manager {} is probably regularly active, dayDifferenceBuffer: {}", manager.getManagerId(),
					dayDifferenceBuffer);
		} else {
			recentLoginFilterCriteriaMatchCount++;
		}

		if (recentLoginFilterCriteriaMatchCount < this.configuration.getLastLoginCriteriaMatch()) {
			LOGGER.debug("Manager {} does not match enough criteria ({}/{})", manager.getManagerId(),
					recentLoginFilterCriteriaMatchCount, this.configuration.getLastLoginCriteriaMatch());
			return false;
		}

		LOGGER.info("Manager {} with {} teams fits the manager criteria", manager.getManagerId(),
				manager.getTeams().size());

		return true;
	}

	public boolean fitsFilterCriteria(Team team) {
		TeamFilterConfiguration teamFilterConfiguration = this.configuration.getTeamFilters().get(team.getSport());

		if (teamFilterConfiguration == null) {
			LOGGER.debug("Manager {}'s team in {} is not in configuration - team ignored",
					team.getManager().getManagerId(), team.getSport());
			return false;
		}

		if (team.getTeamStrength() == null) {
			LOGGER.debug("Manager {}'s team in {} team strengths were not loaded - team ignored",
					team.getManager().getManagerId(), team.getSport());
			return false;
		}

		for (Entry<String, Long> minStrengthConfigEntry : teamFilterConfiguration.getMinTeamStrengths().entrySet()) {
			String teamStrengthAttribute = minStrengthConfigEntry.getKey();
			Long teamStrength = team.getTeamStrength().get(teamStrengthAttribute);
			if (teamStrength == null || teamStrength < minStrengthConfigEntry.getValue()) {
				LOGGER.debug("Manager {}'s team in {} does not match minimum strength criteria ({} {} < {})",
						team.getManager().getManagerId(), team.getSport(), teamStrengthAttribute, teamStrength,
						minStrengthConfigEntry.getValue());
				return false;
			}
		}

		LOGGER.debug("Manager {}'s team in {} fits the criteria", team.getManager().getManagerId(), team.getSport());

		return true;
	}

}
