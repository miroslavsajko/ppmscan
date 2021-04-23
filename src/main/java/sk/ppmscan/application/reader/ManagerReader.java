package sk.ppmscan.application.reader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTable;

import sk.ppmscan.application.beans.Manager;
import sk.ppmscan.application.beans.Sport;
import sk.ppmscan.application.beans.Team;

public final class ManagerReader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ManagerReader.class);
	
	private ManagerReader() {
	}
	
	public static Manager readManagerInfo(HtmlPage page) {
		Manager manager = new Manager();
		manager.setUrl(page.getUrl().toExternalForm());
		manager.setId(getManagerId(page));
		manager.setNickname(getNickname(page));
		manager.setBlocked(getBlocked(page));
		manager.setRecentLogins(getRecentLogins(page));
		manager.setTeams(getTeams(page));
		manager.getTeams().forEach(team -> team.setManager(manager));
		return manager;
	}

	private static long getManagerId(HtmlPage page) {
		HtmlDivision managerIdDiv = page.getFirstByXPath("//div[@class='h1_add_info']");
		String divContent = managerIdDiv.getTextContent();
		String[] splitDivContent = divContent.split(":");
		if (splitDivContent.length < 2) {
			LOGGER.warn("Div with manager ID is weird! Div contains: '{}'; Page url: {}", divContent, page.getUrl());
			return 0;
		}
		return Long.parseLong(splitDivContent[1].trim());
	}

	private static String getNickname(HtmlPage page) {
		HtmlSpan h1Span = page.getFirstByXPath("//h1/span");
		return h1Span.asText();
	}

	private static boolean getBlocked(HtmlPage page) {
		HtmlDivision blockedDiv = page.getFirstByXPath("//div[@class='msg_red']");
		return blockedDiv != null;
	}

	private static List<LocalDateTime> getRecentLogins(HtmlPage page) {
		List<HtmlTable> tables = page.getByXPath("//table[@class='table_profile']").stream()
				.filter(obj -> obj instanceof HtmlTable).map(obj -> (HtmlTable) obj).collect(Collectors.toList());

		if (tables.size() > 1) {
			HtmlTable recentLoginsTable = tables.get(1);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			return recentLoginsTable.getRows().stream().map(htmlRow -> {
				LocalDateTime dateTime = LocalDateTime.parse(htmlRow.asText().trim(), formatter);
				return dateTime;
			}).collect(Collectors.toList());

		}
		return null;
	}

	private static List<Team> getTeams(HtmlPage page) {
		List<?> htmlTeams = page.getByXPath("//div[@class='team_info_profile gray_box']");
		if (CollectionUtils.isEmpty(htmlTeams)) {
			return Collections.emptyList();
		}
		return htmlTeams.stream().map(div -> (HtmlDivision) div).map(ManagerReader::getTeamFromDiv).collect(Collectors.toList());
	}

	private static Team getTeamFromDiv(HtmlDivision teamDiv) {
		Team team = new Team();

		HtmlDivision teamInfoDiv = teamDiv.getFirstByXPath("div[@class='team_info_info']");

		HtmlSpan teamInfoSpan = teamInfoDiv.getFirstByXPath("span");
		team.setSport(Sport.getSportFromDivClass(teamInfoSpan.getAttribute("class")));

		List<HtmlSpan> teamInfoDetailSpans = teamInfoDiv.getByXPath("*/span[@class='team_info_name']").stream()
				.map(span -> (HtmlSpan) span).collect(Collectors.toList());

		HtmlSpan teamNameSpan = teamInfoDetailSpans.get(0);

		HtmlImage teamCountryImage = (HtmlImage) teamNameSpan.getFirstByXPath("*/img");
		team.setTeamCountry(teamCountryImage.getAltAttribute());

		HtmlAnchor teamNameAnchor = teamNameSpan.getFirstByXPath("*/a");
		team.setUrl(teamNameAnchor.getHrefAttribute());
		team.setName(teamNameAnchor.getTextContent());

		HtmlSpan leagueSpan = teamInfoDetailSpans.get(1);

		HtmlImage leagueCountryImage = (HtmlImage) leagueSpan.getFirstByXPath("img");
		team.setLeagueCountry(leagueCountryImage.getAltAttribute());

		HtmlAnchor teamLeagueAnchor = leagueSpan.getFirstByXPath("a");
		team.setLeague(teamLeagueAnchor.getTextContent());

		return team;
	}

}
