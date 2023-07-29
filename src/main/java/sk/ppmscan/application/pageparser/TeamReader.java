package sk.ppmscan.application.pageparser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

import sk.ppmscan.application.beans.Team;

public class TeamReader {

	private TeamReader() {
	}

	/**
	 * Fills in team strengths, all other values are read from the manager page.
	 * 
	 * @param team     team object
	 * @param teamPage fetched page of the team
	 * @return team with filled in team strengths
	 */
	public static Team readTeamInfo(Team team, HtmlPage teamPage) {
		team.setTeamStrength(getTeamStrengths(teamPage));
		return team;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Long> getTeamStrengths(HtmlPage page) {
		HtmlTable teamStrengthTable = page.getFirstByXPath("//table[@class='table_profile']");
		List<HtmlTableRow> rows = (List<HtmlTableRow>) page
				.getByXPath(teamStrengthTable.getCanonicalXPath() + "//tbody//tr[not(@style)]");

		Map<String, Long> teamStrengthMap = new HashMap<>();
		for (HtmlTableRow htmlRow : rows) {
			String strengthName = htmlRow.getCell(0).getTextContent().trim();
			String strength = htmlRow.getCell(1).getTextContent().trim();
			teamStrengthMap.put(strengthName, Long.parseLong(strength));
		}

		return teamStrengthMap;
	}

}
