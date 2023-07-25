package sk.ppmscan.application.beans;

import java.util.Map;
import java.util.Objects;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "team", uniqueConstraints = @UniqueConstraint(columnNames = { Team.COLUMN_NAME_TEAM_ID, "sport", ScanRun.COLUMN_NAME_SCAN_RUN_ID }))
public class Team implements Comparable<Team> {
	
	public static final String COLUMN_NAME_TEAM_ID = "team_id";

	/**
	 * Id of the row in the database. Always unique.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "team_pk", insertable = false, updatable = false)
	private long id;

	/**
	 * Id of the team.
	 */
	@Column(name = COLUMN_NAME_TEAM_ID, nullable = false)
	private Long teamId;

	@ManyToOne
	@JoinColumn(name = ScanRun.COLUMN_NAME_SCAN_RUN_ID)
	private ScanRun scanRun;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Sport sport;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "teamCountry", nullable = false)
	private String teamCountry;

	@Column(name = "league", nullable = false)
	private String league;

	@Column(name = "leagueCountry", nullable = false)
	private String leagueCountry;

	@Column(name = "url", nullable = false)
	private String url;

	@ElementCollection
	@CollectionTable(name = "team_strengths" )
	@MapKeyColumn(name = "strength_type", length = 30) // Specify the column name for the map key
	@Column(name = "strength_value") // Specify the column name for the map value
	private Map<String, Long> teamStrength;

	@ManyToOne()
	@JoinColumn(name = "manager_pk")
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

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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
