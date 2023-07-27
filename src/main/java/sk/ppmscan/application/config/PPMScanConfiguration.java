package sk.ppmscan.application.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import sk.ppmscan.application.beans.Sport;

/**
 * App configuration.
 * 
 * @author miroslavsajko
 *
 */
public class PPMScanConfiguration {

	/**
	 * How many milliseconds between page loads we should wait. Just to be sure.
	 */
	private long millisecondsBetweenPageLoads = 2000;

	/**
	 * How many threads will be used for loading pages.
	 */
	private int sizeOfThreadPool = 10;

	/**
	 * Manager id from which the scan starts.
	 */
	private long managerIdFrom = 0;

	/**
	 * Manager id to which the scan runs.
	 */
	private long managerIdTo = 0;

	/**
	 * A list of manager ids to scan.
	 */
	private List<Long> managerIds = Collections.emptyList();

	/**
	 * Threshold for calculating a manager's activity. For each login entry a
	 * difference between now and the login date is calculated and summed - if the
	 * sum is less than X, he is ignored.
	 */
	private int lastLoginDayDifferenceSumThreshold = 15;

	/**
	 * Threshold for how a manager is considered active. If he logged in less than X
	 * days ago, he is active and skipped.
	 */
	private int lastLoginDaysRecentlyActiveThreshold = 2;

	/**
	 * How many of the last login filter criteria must match to include the manager
	 * in the result.
	 */
	private int lastLoginCriteriaMatch = 2;

	/**
	 * Configuration of a filter for teams. If a sport is not present, the team is
	 * ignored.
	 */
	private Map<Sport, TeamFilterConfiguration> teamFilters = Collections.emptyMap();

	/**
	 * How many managers are processed in one "run".
	 */
	private int chunkSize = 100;

	/**
	 * How many months ago the last login must have happened to add the manager in
	 * the ignore list.
	 */
	private long ignoreListLastLoginMonthsThreshold = 36;

	public long getMillisecondsBetweenPageLoads() {
		return millisecondsBetweenPageLoads;
	}

	public void setMillisecondsBetweenPageLoads(long millisecondsBetweenPageLoads) {
		this.millisecondsBetweenPageLoads = millisecondsBetweenPageLoads;
	}

	public int getSizeOfThreadPool() {
		return sizeOfThreadPool;
	}

	public void setSizeOfThreadPool(int sizeOfThreadPool) {
		this.sizeOfThreadPool = sizeOfThreadPool;
	}

	public long getManagerIdFrom() {
		return managerIdFrom;
	}

	public void setManagerIdFrom(long managerIdFrom) {
		this.managerIdFrom = managerIdFrom;
	}

	public long getManagerIdTo() {
		return managerIdTo;
	}

	public void setManagerIdTo(long managerIdTo) {
		this.managerIdTo = managerIdTo;
	}

	public List<Long> getManagerIds() {
		return managerIds;
	}

	public void setManagerIds(List<Long> managerIds) {
		this.managerIds = managerIds;
	}

	public int getLastLoginDayDifferenceSumThreshold() {
		return lastLoginDayDifferenceSumThreshold;
	}

	public void setLastLoginDayDifferenceSumThreshold(int lastLoginDayDifferenceSumThreshold) {
		this.lastLoginDayDifferenceSumThreshold = lastLoginDayDifferenceSumThreshold;
	}

	public int getLastLoginDaysRecentlyActiveThreshold() {
		return lastLoginDaysRecentlyActiveThreshold;
	}

	public void setLastLoginDaysRecentlyActiveThreshold(int lastLoginDaysRecentlyActiveThreshold) {
		this.lastLoginDaysRecentlyActiveThreshold = lastLoginDaysRecentlyActiveThreshold;
	}

	public int getLastLoginCriteriaMatch() {
		return lastLoginCriteriaMatch;
	}

	public void setLastLoginCriteriaMatch(int lastLoginCriteriaMatch) {
		this.lastLoginCriteriaMatch = lastLoginCriteriaMatch;
	}

	public Map<Sport, TeamFilterConfiguration> getTeamFilters() {
		return teamFilters;
	}

	public void setTeamFilters(Map<Sport, TeamFilterConfiguration> teamFilters) {
		this.teamFilters = teamFilters;
	}

	public int getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	public long getIgnoreListLastLoginMonthsThreshold() {
		return ignoreListLastLoginMonthsThreshold;
	}

	public void setIgnoreListLastLoginMonthsThreshold(long ignoreListLastLoginMonthsThreshold) {
		this.ignoreListLastLoginMonthsThreshold = ignoreListLastLoginMonthsThreshold;
	}

}
