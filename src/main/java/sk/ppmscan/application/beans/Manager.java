package sk.ppmscan.application.beans;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

public class Manager {

	private long id;
	
	/**
	 * True if the user is blocked. Transient means it won't be serialized.
	 */
	private transient boolean blocked;
	
	private String url;

	private String nickname;

	private List<Team> teams;

	private List<LocalDateTime> recentLogins;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Manager: id=");
		builder.append(this.id);
		builder.append(", nickname=");
		builder.append(this.nickname);
		if (CollectionUtils.isNotEmpty(this.teams)) {
			builder.append(", teams: [");
			builder.append(
					this.teams.stream().map(team -> team.getSport().toString()).collect(Collectors.joining(", ")));
			builder.append("]");
		} else {
			builder.append(", no teams");
		}
		if (CollectionUtils.isNotEmpty(this.recentLogins)) {
			builder.append(", recentLogins: [");
			builder.append(
					this.recentLogins.stream().map(loginTime -> loginTime.format(DateTimeFormatter.ISO_DATE_TIME))
							.collect(Collectors.joining(", ")));
			builder.append("]");
		} else {
			builder.append(", no recent logins");
		}
		return builder.toString();
	}

}
