package sk.ppmscan.application.importexport.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.ppmscan.application.beans.Manager;
import sk.ppmscan.application.beans.ScanRun;
import sk.ppmscan.application.beans.Sport;
import sk.ppmscan.application.beans.Team;
import sk.ppmscan.application.importexport.FilteredTeamsExporter;

public class FilteredTeamsExcelExporter implements FilteredTeamsExporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(FilteredTeamsExcelExporter.class);

	@Override
	public void exportData(ScanRun scanRun) throws IOException {
		
		Map<Sport, List<Team>> teams = scanRun.getManagers().stream().map(manager -> manager.getTeams())
				.flatMap(List::stream).collect(Collectors.groupingBy(Team::getSport));
		
		XSSFWorkbook workbook = new XSSFWorkbook();

		for (Entry<Sport, List<Team>> filteredEntry : teams.entrySet()) {
			writeOutputSportToExcelFile(workbook, filteredEntry.getKey(), filteredEntry.getValue());
		}

		DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_DATE)
				.appendLiteral("T").appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral("-")
				.appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral("-")
				.appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();

		String outputFilename = new StringBuilder().append("ppmInactiveManagers-")
				.append(scanRun.getScanTime().format(dateTimeFormatter)).append(".xlsx").toString();

		LOGGER.info("Writing out the result to file {}", outputFilename);
		File outputExcelFile = new File(outputFilename);
		outputExcelFile.createNewFile();
		FileOutputStream outputStream = new FileOutputStream(outputExcelFile);
		workbook.write(outputStream);
		workbook.close();
		LOGGER.info("Writing to the file was successful");

	}

	private void writeOutputSportToExcelFile(XSSFWorkbook workbook, Sport sport, List<Team> teams) {
		XSSFSheet sheet = workbook.createSheet(sport.toString());
		XSSFCreationHelper creationHelper = workbook.getCreationHelper();

		int rowIndex = 0;
		int columnIndex = 0;

		Row firstRow = sheet.createRow(rowIndex++);
		firstRow.createCell(columnIndex).setCellValue("Nickname");
		sheet.setColumnWidth(columnIndex++, 5000);
		firstRow.createCell(columnIndex).setCellValue("Manager URL");
		sheet.setColumnWidth(columnIndex++, 3000);

		firstRow.createCell(columnIndex).setCellValue("Team Name");
		sheet.setColumnWidth(columnIndex++, 5000);
		firstRow.createCell(columnIndex).setCellValue("League");
		sheet.setColumnWidth(columnIndex++, 5000);
		firstRow.createCell(columnIndex).setCellValue("Team URL");
		sheet.setColumnWidth(columnIndex++, 3000);

		for (Team team : teams) {
			Manager manager = team.getManager();

			columnIndex = 0;

			Row row = sheet.createRow(rowIndex++);

			row.createCell(columnIndex++).setCellValue(manager.getNickname());
			Hyperlink managerHyperlink = creationHelper.createHyperlink(HyperlinkType.URL);
			managerHyperlink.setAddress(manager.getUrl());
			Cell managerUrlCell = row.createCell(columnIndex++);
			managerUrlCell.setHyperlink(managerHyperlink);
			managerUrlCell.setCellValue(manager.getUrl());

			row.createCell(columnIndex++).setCellValue(team.getName());
			row.createCell(columnIndex++).setCellValue(team.getLeagueCountry() + " " + team.getLeague());
			XSSFHyperlink teamHyperlink = creationHelper.createHyperlink(HyperlinkType.URL);
			teamHyperlink.setAddress(team.getUrl());
			Cell teamUrlCell = row.createCell(columnIndex++);
			teamUrlCell.setHyperlink(teamHyperlink);
			teamUrlCell.setCellValue(team.getUrl());

			for (Entry<String, Long> teamStrength : team.getTeamStrength().entrySet()) {
				row.createCell(columnIndex).setCellValue(teamStrength.getValue());
				Cell firstRowCell = firstRow.getCell(columnIndex);
				if (firstRowCell == null) {
					firstRowCell = firstRow.createCell(columnIndex);
					firstRowCell.setCellValue(teamStrength.getKey());
					sheet.setColumnWidth(columnIndex, 2000);
				}
				columnIndex++;
			}

			int loginDateIndex = 1;
			for (LocalDateTime loginDate : team.getManager().getRecentLogins()) {
				row.createCell(columnIndex).setCellValue(loginDate.format(DateTimeFormatter.ISO_DATE_TIME));
				Cell firstRowCell = firstRow.getCell(columnIndex);
				if (firstRowCell == null) {
					firstRowCell = firstRow.createCell(columnIndex);
					firstRowCell.setCellValue("Login #" + loginDateIndex++);
					sheet.setColumnWidth(columnIndex, 5000);
				}
				columnIndex++;
			}

		}

	}

	@Override
	public ScanRun importData() throws Exception {
		throw new Exception("Not supported");
	}

}
