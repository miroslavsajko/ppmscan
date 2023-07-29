package sk.ppmscan;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import sk.ppmscan.application.beans.Sport;
import sk.ppmscan.application.beans.Team;
import sk.ppmscan.application.config.PPMScanConfiguration;
import sk.ppmscan.application.config.TeamFilterConfiguration;
import sk.ppmscan.application.filter.ManagerTeamFilterUtility;
import sk.ppmscan.application.pageparser.TeamReader;
import sk.ppmscan.application.processor.FetchManagerTask;
import sk.ppmscan.application.processor.ProcessedManager;

/**
 * Unit test for simple App.
 */
public class AppTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppTest.class);

	@Test
	public void testTeamReader() throws Exception {
		try (WebClient client = new WebClient();) {
			client.getOptions().setCssEnabled(false);
			client.getOptions().setJavaScriptEnabled(false);
			HtmlPage page = client.getPage("https://hockey.powerplaymanager.com/en/team.html?data=125641-predatorsnas");
			Team team = TeamReader.readTeamInfo(new Team(), page);
			for (Entry<String, Long> teamStrength : team.getTeamStrength().entrySet()) {
				LOGGER.info("{}: {}", teamStrength.getKey(), teamStrength.getValue());
			}
		}
	}

	@Test
	public void testFiltering() throws Exception {
		PPMScanConfiguration configuration = new PPMScanConfiguration();

		configuration.setLastLoginCriteriaMatch(0);

		HashMap<Sport, TeamFilterConfiguration> teamFilters = new HashMap<>();

		TeamFilterConfiguration hockeyFilter = new TeamFilterConfiguration();
		HashMap<String, Long> minHockeyTeamStrengths = new HashMap<>();
		minHockeyTeamStrengths.put("Total", 100L);
		hockeyFilter.setMinTeamStrengths(minHockeyTeamStrengths);
		teamFilters.put(Sport.HOCKEY, hockeyFilter);

		TeamFilterConfiguration soccerFilter = new TeamFilterConfiguration();
		HashMap<String, Long> minSoccerTeamStrengths = new HashMap<>();
		minSoccerTeamStrengths.put("Total", 500L);
		soccerFilter.setMinTeamStrengths(minSoccerTeamStrengths);
		teamFilters.put(Sport.SOCCER, soccerFilter);

		configuration.setTeamFilters(teamFilters);

		FetchManagerTask task = new FetchManagerTask(configuration.getMillisecondsBetweenPageLoads(),
				new ManagerTeamFilterUtility(configuration, LocalDateTime.now()),
				configuration.getTeamFilters().keySet(), 17909);

		Entry<Long, ProcessedManager> entry = task.call();
		Assert.assertNotNull(entry);
		Assert.assertNotNull(entry.getValue());
		Assert.assertNotNull(entry.getValue().getManager());
		Assert.assertNotNull(entry.getValue().getManager().getTeams());
		Assert.assertFalse(entry.getValue().getManager().getTeams().isEmpty());
		Assert.assertEquals(1, entry.getValue().getManager().getTeams().size());

	}

}
