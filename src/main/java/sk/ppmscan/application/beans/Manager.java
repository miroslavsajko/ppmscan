package sk.ppmscan.application.beans;

import java.time.LocalDateTime;
import java.util.List;

public class Manager {

	private Long managerId;

	/**
	 * True if the user is blocked. Transient means it won't be serialized.
	 */
	private transient boolean blocked;

	private String url;

	private String nickname;

	private List<Team> teams;

	private List<LocalDateTime> recentLogins;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public List<Team> getTeams() {
		return teams;
	}

	public void setTeams(List<Team> teams) {
		this.teams = teams;
	}

	public List<LocalDateTime> getRecentLogins() {
		return recentLogins;
	}

	public void setRecentLogins(List<LocalDateTime> recentLogins) {
		this.recentLogins = recentLogins;
	}

	public boolean isBlocked() {
		return blocked;
	}

	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

	public Long getManagerId() {
		return managerId;
	}

	public void setManagerId(Long managerId) {
		this.managerId = managerId;
	}

}
