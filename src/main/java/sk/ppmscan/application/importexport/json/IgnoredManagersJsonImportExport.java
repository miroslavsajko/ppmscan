package sk.ppmscan.application.importexport.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.stream.JsonWriter;

import sk.ppmscan.application.PPMScannerApplication;
import sk.ppmscan.application.importexport.IgnoredManagersImportExport;

public class IgnoredManagersJsonImportExport implements IgnoredManagersImportExport {

	private static final Logger LOGGER = LoggerFactory.getLogger(PPMScannerApplication.class);

	private static final String IGNORED_MANAGERS_FILENAME = "ignoredManagers.json";

	@SuppressWarnings("unchecked")
	public Set<Long> importData() {
		LOGGER.info("Importing ignored managers from a json file");
		long startTimestamp = System.currentTimeMillis();
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
		LOGGER.info("The operation took {} ms", System.currentTimeMillis() - startTimestamp);
		return readIgnoredManagers;
	}

	public void exportData(Set<Long> updatedIgnoredManagers) throws IOException {
		LOGGER.info("Exporting ignored managers to a json file");
		long startTime = System.currentTimeMillis();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		File outputJsonFile = new File(IGNORED_MANAGERS_FILENAME);
		outputJsonFile.createNewFile();
		JsonWriter jsonWriter = gson.newJsonWriter(new FileWriter(outputJsonFile));
		gson.toJson(gson.toJsonTree(updatedIgnoredManagers), jsonWriter);
		jsonWriter.close();
		LOGGER.info("The operation took {} ms", System.currentTimeMillis() - startTime);
	}

}
