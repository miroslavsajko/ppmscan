package sk.ppmscan.application.beans;

import java.util.Map;
import java.util.Objects;

public class Team implements Comparable<Team> {
	
	/**
	 * Id of the team.
	 */
	private Long teamId;

	private ScanRun scanRun;

	private Sport sport;

	private String name;

	private String teamCountry;

	private String league;

	private String leagueCountry;

	private String url;

	private Map<String, Long> teamStrength;

	private Manager manager;

	public Map<String, Long> getTeamStrength() {
		return teamStrength;
	}

	public Manager getManager() {
		return manager;
	}

	public void setManager(Manager manager) {
		this.manager = manager;
	}

	public void setTeamStrength(Map<String, Long> teamStrength) {
		this.teamStrength = teamStrength;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTeamCountry() {
		return teamCountry;
	}

	public void setTeamCountry(String teamCountry) {
		this.teamCountry = teamCountry;
	}

	public String getLeague() {
		return league;
	}

	public void setLeague(String league) {
		this.league = league;
	}

	public String getLeagueCountry() {
		return leagueCountry;
	}

	public void setLeagueCountry(String leagueCountry) {
		this.leagueCountry = leagueCountry;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Long getTeamId() {
		return teamId;
	}

	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}

	public ScanRun getScanRun() {
		return scanRun;
	}

	public void setScanRun(ScanRun scanRun) {
		this.scanRun = scanRun;
	}

	public Sport getSport() {
		return sport;
	}

	public void setSport(Sport sport) {
		this.sport = sport;
	}

	@Override
	public int hashCode() {
		return Objects.hash(scanRun.getScanTime(), sport, teamId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Team other = (Team) obj;
		return Objects.equals(scanRun.getScanTime(), other.scanRun.getScanTime()) && sport == other.sport && Objects.equals(teamId, other.teamId);
	}

	@Override
	public int compareTo(Team o) {
		// comparing is only for the insertion in a map, where teams are split between
		// sports, so this is enough
		return this.getTeamId().compareTo(o.getTeamId());
	}

}
