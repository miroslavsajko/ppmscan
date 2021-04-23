package sk.ppmscan.application.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.stream.JsonWriter;

public class ConfigurationHolder {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationHolder.class);

	private static final String CONFIG_FILENAME = "PPMScanConfig.json";

	private static final String IGNORED_MANAGERS_FILENAME = "ignoredManagers.json";

	private static ConfigurationHolder configHolder = null;

	private Configuration configuration;

	private Set<Long> ignoredManagers;

	private ConfigurationHolder() throws Exception {
		this.configuration = validateConfiguration(readConfiguration());
		this.ignoredManagers = readIgnoredManagers();
	}

	private static Configuration readConfiguration() throws Exception {
		Configuration configuration = null;
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try {
			FileReader json = new FileReader(CONFIG_FILENAME);
			configuration = gson.fromJson(json, Configuration.class);
			LOGGER.info("Configuration was successfully read");
		} catch (FileNotFoundException e) {
			LOGGER.info("Configuration file was not found");
		}
		if (configuration == null) {
			File configFile = new File(CONFIG_FILENAME);
			configFile.createNewFile();
			FileWriter writer = new FileWriter(configFile);
			JsonWriter jsonWriter = gson.newJsonWriter(writer);
			configuration = new Configuration();
			gson.toJson(gson.toJsonTree(configuration), jsonWriter);
			jsonWriter.close();
			writer.close();
			LOGGER.info("New configuration file with default values was created");
		}
		return configuration;
	}

	@SuppressWarnings("unchecked")
	private static Set<Long> readIgnoredManagers() {
		Set<Long> readIgnoredManagers;
		Gson gson = new GsonBuilder().setPrettyPrinting().setLongSerializationPolicy(LongSerializationPolicy.DEFAULT)
				.create();
		try {
			FileReader json = new FileReader(IGNORED_MANAGERS_FILENAME);
			readIgnoredManagers = (Set<Long>) gson.fromJson(json, Set.class).stream().map(a -> {
				return Double.valueOf("" + a).longValue();
			}).collect(Collectors.toSet());
			LOGGER.info("{} ignored managers were read successfully from a file.", readIgnoredManagers.size());
		} catch (FileNotFoundException e) {
			LOGGER.info("Ignored managers file was not found. Continue with an empty one.");
			readIgnoredManagers = Collections.emptySet();
		}
		return readIgnoredManagers;
	}

	private static Configuration validateConfiguration(Configuration configuration) {
		if (configuration.getManagerIdFrom() > configuration.getManagerIdTo()) {
			long temp = configuration.getManagerIdFrom();
			configuration.setManagerIdFrom(configuration.getManagerIdTo());
			configuration.setManagerIdTo(temp);
		}
		if (configuration.getManagerIds() == null) {
			configuration.setManagerIds(new LinkedList<>());
		}
		if (configuration.getSizeOfThreadPool() < 1 || configuration.getSizeOfThreadPool() > 30) {
			configuration.setSizeOfThreadPool(10);
		}
		if (configuration.getExportFormat() == null) {
			configuration.setExportFormat(ExportFormat.EXCEL);
		}
		if (configuration.getMillisecondsBetweenPageLoads() < 100) {
			configuration.setMillisecondsBetweenPageLoads(100);
		}
		if (configuration.getIgnoreListLastLoginMonthsThreshold() < 0 || configuration.getIgnoreListLastLoginMonthsThreshold() > 360) {
			configuration.setIgnoreListLastLoginMonthsThreshold(36);
		}
		return configuration;
	}

	public static ConfigurationHolder getInstance() throws Exception {
		if (configHolder == null)
			configHolder = new ConfigurationHolder();

		return configHolder;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public Set<Long> getIgnoredManagers() {
		return ignoredManagers;
	}

}
