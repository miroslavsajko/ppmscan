package sk.ppmscan;

import java.util.Map.Entry;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import sk.ppmscan.application.beans.Team;
import sk.ppmscan.application.pageparser.TeamReader;

/**
 * Unit test for simple App.
 */
public class AppTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppTest.class);

	@Test
	public void testTeamReader() throws Exception {
		WebClient client = new WebClient();
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
		HtmlPage page = client.getPage("https://hockey.powerplaymanager.com/en/team.html?data=125641-predatorsnas");
		Team team = TeamReader.readTeamInfo(new Team(), page);
		for (Entry<String, Long> teamStrength : team.getTeamStrength().entrySet()) {
			LOGGER.info("{}: {}", teamStrength.getKey(), teamStrength.getValue());
		}
		client.close();
	}
	
}
