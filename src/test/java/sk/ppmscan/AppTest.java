package sk.ppmscan;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import sk.ppmscan.application.beans.Team;
import sk.ppmscan.application.importexport.IgnoredManagersImportExport;
import sk.ppmscan.application.importexport.json.IgnoredManagersJsonImportExport;
import sk.ppmscan.application.importexport.sqlite.IgnoredManagersSQliteImportExport;
import sk.ppmscan.application.pageparser.TeamReader;

/**
 * Unit test for simple App.
 */
public class AppTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppTest.class);
	@Test
	public void shouldAnswerWithTrue() throws SQLException {
		assertTrue(true);
	}
	
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
	
	@Ignore
	@Test
	public void ignoredManagersImportTesting() throws Exception {
		
		IgnoredManagersImportExport jsonImportExport = new IgnoredManagersJsonImportExport();
		IgnoredManagersImportExport sqLiteImportExport = new IgnoredManagersSQliteImportExport();
		
		Set<Long> jsonSet = jsonImportExport.importData();
		LOGGER.info("**************");
		Set<Long> sqliteSet = sqLiteImportExport.importData();
		LOGGER.info("**************");
		
//		Long newManagerId = new Random().nextLong();
//		jsonSet.add(newManagerId);
//		sqliteSet.add(newManagerId);
		
		jsonImportExport.exportData(jsonSet);
		LOGGER.info("**************");
		sqLiteImportExport.exportData(sqliteSet);
		
	}
	
	@Ignore
	@Test
	public void copyIgnoreManagersFromJsonToSqlite() throws Exception {
		IgnoredManagersImportExport jsonImportExport = new IgnoredManagersJsonImportExport();
		Set<Long> jsonImported = jsonImportExport.importData();
		IgnoredManagersImportExport sqLiteImportExport = new IgnoredManagersSQliteImportExport();
		sqLiteImportExport.exportData(jsonImported);
	}
	
	@Ignore
	@Test
	public void copyIgnoreManagersFromSqliteToJson() throws Exception {
		IgnoredManagersImportExport sqLiteImportExport = new IgnoredManagersSQliteImportExport();
		Set<Long> imported = sqLiteImportExport.importData();
		
		IgnoredManagersImportExport jsonImportExport = new IgnoredManagersJsonImportExport();
		jsonImportExport.exportData(imported);
		
	}
	
}
