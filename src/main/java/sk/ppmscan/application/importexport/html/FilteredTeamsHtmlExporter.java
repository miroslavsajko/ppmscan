package sk.ppmscan.application.importexport.html;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.ppmscan.application.beans.ScanRun;
import sk.ppmscan.application.beans.Sport;
import sk.ppmscan.application.beans.Team;
import sk.ppmscan.application.importexport.FilteredTeamsExporter;

public class FilteredTeamsHtmlExporter implements FilteredTeamsExporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(FilteredTeamsHtmlExporter.class);

	@Override
	public void exportData(ScanRun scanRun) throws IOException {
		DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_DATE)
				.appendLiteral("T").appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral("-")
				.appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral("-")
				.appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();

		String outputFilename = new StringBuilder().append("ppmInactiveManagers-")
				.append(scanRun.getScanTime().format(dateTimeFormatter)).append(".html").toString();

		Map<Sport, List<Team>> teams = scanRun.getManagers().stream().map(manager -> manager.getTeams())
				.flatMap(List::stream).collect(Collectors.groupingBy(Team::getSport));

		LOGGER.info("Writing out the result to file: {}", outputFilename);

		File outputHtmlFile = new File(outputFilename);
		outputHtmlFile.createNewFile();

		BufferedWriter writer = new BufferedWriter(new FileWriter(outputHtmlFile));

		writer.write("<html>");

		writer.write("<head>");
		writer.write("<style>");
		writer.write("table { border: 1px solid black; border-collapse: collapse; }");
		writer.write("td { border: 1px solid black; padding: 5px; }");
		writer.write(".bold { font-weight: bold; }");
		writer.write(".nickname { width: 175px; }");
		writer.write(".teamname { width: 250px; }");
		writer.write(".league { width: 160px; }");
		writer.write(".strength { width: 40px; }");
		writer.write(".logindate { width: 145px; }");
		writer.write("span { margin: 16px; }");
		writer.write("</style>");
		writer.write("</head>");

		writer.write("<body>");

		for (Entry<Sport, List<Team>> entry : teams.entrySet()) {
			writer.write("<div><span class=\"bold\">Sport: " + entry.getKey().toString() + "</span>");
			writer.write("<br/>");

			writer.write("<table>");

			writer.write("<tr>");

			writer.write("<td class=\"nickname bold\">");
			writer.write("Nickname");
			writer.write("</td>");

			writer.write("<td class=\"teamname bold\">");
			writer.write("Team Name");
			writer.write("</td>");

			writer.write("<td class=\"league bold\">");
			writer.write("League");
			writer.write("</td>");

			Optional<Team> optionalTeam = entry.getValue().stream().findFirst();
			if (optionalTeam.isPresent()) {
				Team team = optionalTeam.get();
				for (String teamAttribute : team.getTeamStrength().keySet()) {
					writer.write("<td class=\"strength bold\">");
					writer.write(teamAttribute.substring(0, 3));
					writer.write("</td>");
				}

				for (int i = 1; i <= 5; i++) {
					writer.write("<td class=\"logindate bold\">");
					writer.write("Login#" + i);
					writer.write("</td>");
				}
			}

			writer.write("</tr>");

			for (Team team : entry.getValue()) {
				writer.write("<tr>");

				writer.write("<td>");
				writer.write(
						"<a href=\"" + team.getManager().getUrl() + "\">" + team.getManager().getNickname() + "</a>");
				writer.write("</td>");

				writer.write("<td>");
				writer.write("<a href=\"" + team.getUrl() + "\">" + team.getName() + "</a>");
				writer.write("</td>");

				writer.write("<td>");
				writer.write(team.getLeagueCountry() + " " + team.getLeague());
				writer.write("</td>");

				for (Entry<String, Long> teamStrength : team.getTeamStrength().entrySet()) {
					writer.write("<td>");
					writer.write(teamStrength.getValue() + "");
					writer.write("</td>");
				}

				for (LocalDateTime loginDate : team.getManager().getRecentLogins()) {
					writer.write("<td>");
					writer.write(loginDate.format(DateTimeFormatter.ISO_DATE_TIME));
					writer.write("</td>");
				}

				writer.write("</tr>");
			}
			writer.write("</table></div>");

			writer.write("<br/>");
		}

		writer.write("</body></html>");
		writer.close();
		LOGGER.info("Writing to the file was successful");

	}

	@Override
	public ScanRun importData() throws Exception {
		throw new Exception("Not supported");
	}

}
