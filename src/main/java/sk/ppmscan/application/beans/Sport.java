package sk.ppmscan.application.beans;

public enum Sport {

	HOCKEY("hockey_team_info_name_sport"),

	SOCCER("soccer_team_info_name_sport"),

	HANDBALL("handball_team_info_name_sport"),

	BASKETBALL("basketball_team_info_name_sport");

	private String divClass;

	private Sport(String divClass) {
		this.divClass = divClass;
	}

	public String getDivClass() {
		return divClass;
	}

	public static Sport getSportFromDivClass(String divClass) /*throws Exception*/ {
		for (Sport sportType : values()) {
			if (sportType.getDivClass().equals(divClass)) {
				return sportType;
			}
		}
		return null;
//		throw new Exception("Unknown div class");
	}

}
