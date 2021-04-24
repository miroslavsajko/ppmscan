/**
 * 
 */
package sk.ppmscan.application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.Calendar;
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
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
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
import sk.ppmscan.application.config.TeamFilterConfiguration;
import sk.ppmscan.application.export.ExcelExporter;
import sk.ppmscan.application.export.HtmlExporter;
import sk.ppmscan.application.export.IExporter;
import sk.ppmscan.application.export.JsonExporter;
import sk.ppmscan.application.reader.ManagerReader;
import sk.ppmscan.application.reader.TeamReader;
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
		long timeStampBefore = Calendar.getInstance().getTimeInMillis();
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

			IExporter exporter;
			switch (configuration.getExportFormat()) {
			case EXCEL:
				exporter = new ExcelExporter(this.now);
				break;
			case HTML:
				exporter = new HtmlExporter(this.now);
				break;
			case JSON:
			default:
				exporter = new JsonExporter(this.now);
				break;
			}
			exporter.export(filteredTeams);

			i += configuration.getChunkSize();
		}
		long timeStampAfter = Calendar.getInstance().getTimeInMillis();
		Duration duration = new Duration(timeStampAfter - timeStampBefore);

		PeriodFormatter formatter = new PeriodFormatterBuilder().appendHours().appendSuffix("h ").appendMinutes()
				.appendSuffix("m ").appendSeconds().appendSuffix("s").toFormatter();
		String formatted = formatter.print(duration.toPeriod());

		LOGGER.info("The scanning took: {}", formatted);

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
