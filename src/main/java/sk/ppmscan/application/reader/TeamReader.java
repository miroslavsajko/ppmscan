package sk.ppmscan.application.reader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

import sk.ppmscan.application.beans.Team;

public class TeamReader {

	//private static final Logger LOGGER = LoggerFactory.getLogger(TeamReader.class);

	private TeamReader() {
	}

	public static Team readTeamInfo(Team team, HtmlPage page) {
		team.setTeamStrength(getTeamStrengths(page));
		return team;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Long> getTeamStrengths(HtmlPage page) {
		HtmlTable teamStrengthTable = page.getFirstByXPath("//table[@class='table_profile']");
		List<HtmlTableRow> rows = (List<HtmlTableRow>) page.getByXPath(teamStrengthTable.getCanonicalXPath()+"//tbody//tr[not(@style)]");

		Map<String, Long> teamStrengthMap = new HashMap<>();
		for (HtmlTableRow htmlRow : rows) {
			String strengthName = htmlRow.getCell(0).getTextContent().trim();
			String strength = htmlRow.getCell(1).getTextContent().trim();
			teamStrengthMap.put(strengthName, Long.parseLong(strength));
		}

		return teamStrengthMap;
	}

}
