package sk.ppmscan.application.beans;

import java.util.Map;

public class Team implements Comparable<Team> {

	private String name;

	private String teamCountry;

	private String league;

	private String leagueCountry;

	private String url;

	private Sport sport;

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

	public Sport getSport() {
		return sport;
	}

	public void setSport(Sport sport) {
		this.sport = sport;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Team: name=");
		builder.append(name);
		builder.append(", sport=");
		builder.append(sport);
		builder.append(", teamCountry=");
		builder.append(teamCountry);
		builder.append(", leagueCountry=");
		builder.append(leagueCountry);
		builder.append(", league=");
		builder.append(league);
		builder.append(", url= ");
		builder.append(url);
		return builder.toString();
	}

	@Override
	public int compareTo(Team other) {
		if (this.getTeamStrength() != null && other.getTeamStrength() != null) {
			return -this.getTeamStrength().getOrDefault("Total", this.getManager().getId())
					.compareTo(other.getTeamStrength().getOrDefault("Total", other.getManager().getId()));
		}
		return Long.valueOf(this.getManager().getId()).compareTo(Long.valueOf(other.getManager().getId()));
	}

}
