package sk.ppmscan.application.config;

import java.util.Map;

public class TeamFilterConfiguration {
	
	private Map<String, Long> minTeamStrengths;

	public Map<String, Long> getMinTeamStrengths() {
		return minTeamStrengths;
	}

	public void setMinTeamStrengths(Map<String, Long> minTeamStrengths) {
		this.minTeamStrengths = minTeamStrengths;
	}

}
