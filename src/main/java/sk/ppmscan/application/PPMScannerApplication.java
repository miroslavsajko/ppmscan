/**
 * 
 */
package sk.ppmscan.application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
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

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import sk.ppmscan.application.beans.Manager;
import sk.ppmscan.application.beans.Sport;
import sk.ppmscan.application.beans.Team;
import sk.ppmscan.application.config.Configuration;
import sk.ppmscan.application.config.ExportFormat;
import sk.ppmscan.application.config.TeamFilterConfiguration;
import sk.ppmscan.application.reader.ManagerReader;
import sk.ppmscan.application.reader.TeamReader;
import sk.ppmscan.application.util.LocalDateTimeJsonSerializer;
import sk.ppmscan.application.util.PPMScannerThreadPoolExecutor;

/**
 * 
 * @author mirak
 *
 */
public class PPMScannerApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(PPMScannerApplication.class);

	private static final String MANAGER_PROFILE_URL = "https://ppm.powerplaymanager.com/en/manager-profile.html?data=";

	private static final String LOGIN_URL = "https://ppm.powerplaymanager.com/en/login-to-ppm.html";

	private static final String IGNORED_MANAGERS_FILENAME = "ignoredManagers.json";

	private BrowserVersion[] browsers = new BrowserVersion[] { BrowserVersion.CHROME, BrowserVersion.EDGE,
			BrowserVersion.FIREFOX_38, BrowserVersion.INTERNET_EXPLORER_11 };

	private LocalDateTime now;

	private Configuration configuration;

	private Set<Long> ignoredManagers;

	public PPMScannerApplication(Configuration configuration, Set<Long> ignoredManagers, String login, String password)
			throws Exception {
		this.now = LocalDateTime.now();
		this.configuration = configuration;
		this.ignoredManagers = ignoredManagers;
	}

	public PPMScannerApplication(Configuration configuration, Set<Long> ignoredManagers) throws Exception {
		this.now = LocalDateTime.now();
		this.configuration = configuration;
		this.ignoredManagers = ignoredManagers;
	}

	public void scan() throws Exception {
		List<Long> managerIds = configuration.getManagerIds().stream().collect(Collectors.toList());

		for (long managerId = configuration.getManagerIdFrom(); managerId <= configuration
				.getManagerIdTo(); managerId++) {
			managerIds.add(managerId);
		}
		LOGGER.info("{} managers will be scanned", managerIds.size());
		LOGGER.info("Applying ignore list of {} managers", ignoredManagers.size());

		managerIds.removeAll(ignoredManagers);
		managerIds.sort((a, b) -> a.compareTo(b));

		LOGGER.info("After applying ignore list: {} managers will be scanned", managerIds.size());

		Set<Long> ignoredManagerSet = Collections.synchronizedSortedSet(new TreeSet<Long>(this.ignoredManagers));
		Set<Manager> filteredManagers = Collections.synchronizedSet(new HashSet<>());
		Map<Sport, Set<Team>> filteredTeams = Collections.synchronizedMap(new TreeMap<Sport, Set<Team>>());
		this.configuration.getTeamFilters().keySet()
				.forEach(sport -> filteredTeams.put(sport, Collections.synchronizedSet(new TreeSet<>())));

		int sizeOfThreadPool = configuration.getSizeOfThreadPool();
		LOGGER.info("{} threads will be used", sizeOfThreadPool);
		long timeStampBefore = System.currentTimeMillis();
		ExecutorService executorService = new PPMScannerThreadPoolExecutor(sizeOfThreadPool, managerIds.size());

		List<Callable<Entry<Long, Manager>>> tasks = new LinkedList<Callable<Entry<Long, Manager>>>();

		for (int i = 0; i < managerIds.size();) {

			for (int j = i; j < i + configuration.getChunkSize() && j < managerIds.size(); j++) {
				Long managerId = managerIds.get(j);

				tasks.add(new Callable<Entry<Long, Manager>>() {

					@Override
					public Entry<Long, Manager> call() {
						try {
							WebClient client = createWebClient();
							HtmlPage managerPage;
							managerPage = client.getPage(MANAGER_PROFILE_URL + managerId);
							Thread.sleep(configuration.getMillisecondsBetweenPageLoads());
							Manager manager = ManagerReader.readManagerInfo(managerPage);
							if (isIgnorable(manager)) {
								ignoredManagerSet.add(managerId);
							} else if (applyManagerFilters(manager)) {
								filteredManagers.add(manager);
								for (Team team : manager.getTeams()) {
									TeamReader.readTeamInfo(team, client.getPage(team.getUrl()));
									if (applyTeamFilters(team)) {
										filteredTeams.get(team.getSport()).add(team);
									}
									Thread.sleep(configuration.getMillisecondsBetweenPageLoads());
								}
							}
							client.close();
							return new AbstractMap.SimpleEntry<Long, Manager>(managerId, manager);
						} catch (Exception e) {
							LOGGER.error("Error during loading of manager  " + managerId, e);
							return new AbstractMap.SimpleEntry<Long, Manager>(managerId, null);
						}
					}
				});
			}

			int ignoreListSizeBefore = ignoredManagerSet.size();
//			List<Future<Entry<Long, Manager>>> futures = executorService.invokeAll(tasks);
			executorService.invokeAll(tasks);
			int ignoreListSizeAfter = ignoredManagerSet.size();

			tasks.clear();

			LOGGER.info("Added another {} managers to the ignore list, ignore list now contains {} managers",
					ignoreListSizeAfter - ignoreListSizeBefore, ignoredManagerSet.size());
			this.writeIgnoredManagersToFile(ignoredManagerSet);

			LOGGER.info("Found {} managers in this run", filteredManagers.size());
			for (Entry<Sport, Set<Team>> filteredTeamEntry : filteredTeams.entrySet()) {
				LOGGER.info("Found {} teams in {}", filteredTeamEntry.getValue().size(), filteredTeamEntry.getKey());
			}

			if (ExportFormat.JSON.equals(configuration.getExportFormat())) {
				this.exportToJsonFile(filteredTeams);
			}
			if (ExportFormat.EXCEL.equals(configuration.getExportFormat())) {
				this.exportToExcelFile(filteredTeams);
			}

			i += configuration.getChunkSize();
		}

		long timeStampAfter = System.currentTimeMillis();
		Duration duration = Duration.ofMillis(timeStampAfter - timeStampBefore);
		long hours = duration.get(ChronoUnit.SECONDS) / 3600;
		long minutes = (duration.get(ChronoUnit.SECONDS) - (hours * 3600)) / 60;
		long seconds = (duration.get(ChronoUnit.SECONDS) - (hours * 3600) - (minutes * 60)) / 60;
		LOGGER.info("The scanning took {}h {}m {}s", hours, minutes, seconds);

		executorService.awaitTermination(5, TimeUnit.SECONDS);
		LOGGER.info("{} tasks never finished because of termination", executorService.shutdownNow().size());

	}

	/**
	 * Manager is ignorable when he is blocked or when he has no recent logins.
	 * 
	 * @param manager manager
	 * @return true if this manager should be ignored
	 */
	private boolean isIgnorable(Manager manager) {
		if (manager.isBlocked()) {
			LOGGER.debug("Manager {} is blocked", manager.getId());
			return true;
		}
		if (CollectionUtils.isEmpty(manager.getRecentLogins())) {
			LOGGER.debug("Manager {} doesnt have any recent logins", manager.getId());
			return true;
		}
		long lastLoginMonthsAgo = ChronoUnit.MONTHS.between(manager.getRecentLogins().get(0), this.now);
		if (lastLoginMonthsAgo > this.configuration.getIgnoreListLastLoginMonthsThreshold()) {
			LOGGER.debug("Manager {} logged in the last time {} months ago", manager.getId(), lastLoginMonthsAgo);
			return true;
		}
		return false;
	}

	private boolean applyManagerFilters(Manager manager) {
		if (CollectionUtils.isEmpty(manager.getTeams())) {
			LOGGER.debug("Manager {} has no teams", manager.getId());
			return false;
		}

		int recentLoginFilterCriteriaMatchCount = 0;

		LocalDateTime mostRecentLogin = manager.getRecentLogins().get(0);

		long mostRecentLoginDaysAgo = ChronoUnit.DAYS.between(mostRecentLogin, this.now);
		if (mostRecentLoginDaysAgo < configuration.getLastLoginDaysRecentlyActiveThreshold()) {
			LOGGER.debug("Manager {} was active just recently", manager.getId());
		} else {
			recentLoginFilterCriteriaMatchCount++;
		}

		long dayDifferenceBuffer = 0;
		for (int i = 0; i < manager.getRecentLogins().size() - 1; i++) {
			long loginDayDifference = ChronoUnit.DAYS.between(manager.getRecentLogins().get(i), this.now);
			dayDifferenceBuffer += loginDayDifference;
		}

		if (dayDifferenceBuffer < configuration.getLastLoginDayDifferenceSumThreshold()) {
			// the user is probably regularly active
			LOGGER.debug("Manager {} is probably regularly active, dayDifferenceBuffer: {}", manager.getId(),
					dayDifferenceBuffer);
		} else {
			recentLoginFilterCriteriaMatchCount++;
		}

		if (recentLoginFilterCriteriaMatchCount < this.configuration.getLastLoginCriteriaMatch()) {
			LOGGER.debug("Manager {} does not match enough criteria ({}/{})", manager.getId(),
					recentLoginFilterCriteriaMatchCount, this.configuration.getLastLoginCriteriaMatch());
			return false;
		}

		LOGGER.info("Manager {} with {} teams fits the manager criteria", manager.getId(), manager.getTeams().size());
		return true;
	}

	private boolean applyTeamFilters(Team team) {
		TeamFilterConfiguration teamFilterConfiguration = this.configuration.getTeamFilters().get(team.getSport());
		if (teamFilterConfiguration == null) {
			LOGGER.info("Manager {}'s team in {} is not in configuration - team ignored", team.getManager().getId(),
					team.getSport());
			return false;
		}
		for (Entry<String, Long> minStrengthConfigEntry : teamFilterConfiguration.getMinTeamStrengths().entrySet()) {
			String teamStrengthAttribute = minStrengthConfigEntry.getKey();
			Long teamStrength = team.getTeamStrength().get(teamStrengthAttribute);
			if (teamStrength == null || teamStrength < minStrengthConfigEntry.getValue()) {
				LOGGER.info("Manager {}'s team in {} does not match minimum strength criteria ({} {} < {})",
						team.getManager().getId(), team.getSport(), teamStrengthAttribute, teamStrength,
						minStrengthConfigEntry.getValue());
				return false;
			}
		}
		LOGGER.info("Manager {}'s team in {} fits the criteria", team.getManager().getId(), team.getSport());
		return true;
	}

	private void writeIgnoredManagersToFile(Collection<Long> updatedIgnoredManagers) throws IOException {
		LOGGER.info("Writing out ignored managers to file: {}", IGNORED_MANAGERS_FILENAME);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		File outputJsonFile = new File(IGNORED_MANAGERS_FILENAME);
		outputJsonFile.createNewFile();
		JsonWriter jsonWriter = gson.newJsonWriter(new FileWriter(outputJsonFile));
		gson.toJson(gson.toJsonTree(updatedIgnoredManagers), jsonWriter);
		jsonWriter.close();
		LOGGER.info("Writing to the file was successful");

	}

	private void exportToExcelFile(Map<Sport, Set<Team>> filteredTeams) throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook();

		for (Entry<Sport, Set<Team>> filteredEntry : filteredTeams.entrySet()) {
			writeOutputSportToExcelFile(workbook, filteredEntry.getKey(), filteredEntry.getValue());
		}

		DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_DATE)
				.appendLiteral("T").appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral("-")
				.appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral("-")
				.appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();

		String outputFilename = new StringBuilder().append("ppmInactiveManagers-")
				.append(this.now.format(dateTimeFormatter)).append(".xlsx").toString();

		LOGGER.info("Writing out the result to file {}", outputFilename);
		File outputExcelFile = new File(outputFilename);
		outputExcelFile.createNewFile();
		FileOutputStream outputStream = new FileOutputStream(outputExcelFile);
		workbook.write(outputStream);
		workbook.close();
		LOGGER.info("Writing to the file was successful");
	}

	private void writeOutputSportToExcelFile(XSSFWorkbook workbook, Sport sport, Set<Team> teams) {
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

			for (int i = 0; i < manager.getRecentLogins().size(); i++) {
				LocalDateTime loginDate = manager.getRecentLogins().get(i);
				row.createCell(columnIndex).setCellValue(loginDate.format(DateTimeFormatter.ISO_DATE_TIME));
				Cell firstRowCell = firstRow.getCell(columnIndex);
				if (firstRowCell == null) {
					firstRowCell = firstRow.createCell(columnIndex);
					firstRowCell.setCellValue("Login #" + i);
					sheet.setColumnWidth(columnIndex, 5000);
				}
				columnIndex++;
			}

		}

	}

	private void exportToJsonFile(Map<Sport, Set<Team>> filteredTeams) throws IOException {
		DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_DATE)
				.appendLiteral("T").appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral("-")
				.appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral("-")
				.appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();

		String outputFilename = new StringBuilder().append("ppmInactiveManagers-")
				.append(this.now.format(dateTimeFormatter)).append(".json").toString();

		LOGGER.info("Writing out the result to file: {}", outputFilename);

		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
				.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeJsonSerializer()).create();
		File outputJsonFile = new File(outputFilename);
		outputJsonFile.createNewFile();
		JsonWriter jsonWriter = gson.newJsonWriter(new FileWriterWithEncoding(outputJsonFile, "UTF-8"));
		gson.toJson(gson.toJsonTree(filteredTeams), jsonWriter);
		jsonWriter.close();
		LOGGER.info("Writing to the file was successful");
	}

	private WebClient createWebClient() throws Exception {
		WebClient client = new WebClient(browsers[new Random().nextInt(browsers.length)]);
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
		return client;
	}

	@SuppressWarnings("unused")
	private WebClient createWebClient(String login, String password) throws Exception {
		WebClient client = createWebClient();

		HtmlPage page = client.getPage(LOGIN_URL);

		HtmlInput inputUsername = page.getFirstByXPath("//input[@id='username']");
		if (inputUsername == null) {
			throw new Exception("Username field not found");
		}

		HtmlInput inputPassword = page.getFirstByXPath("//input[@type='password']");
		if (inputPassword == null) {
			throw new Exception("Password field not found");
		}

		inputUsername.setValueAttribute(login);
		inputPassword.setValueAttribute(password);

		// get the enclosing form
		HtmlForm loginForm = inputPassword.getEnclosingForm();

		// submit the form
		page = client.getPage(loginForm.getWebRequest(null));

		page = client.getPage("https://ppm.powerplaymanager.com/en/home.html");

		LOGGER.info("After login, title text: " + page.getTitleText());
		if (!"User account".equals(page.getTitleText())) {
			throw new Exception("Most likely not logged in");
		}

		LOGGER.info("Logged in!");

		// returns the cookies filled client :)
		return client;
	}

}
