package sk.ppmscan.application.importexport.json;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import sk.ppmscan.application.beans.ScanRun;
import sk.ppmscan.application.importexport.FilteredTeamsExporter;

public class FilteredTeamsJsonExporter implements FilteredTeamsExporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(FilteredTeamsJsonExporter.class);

	@Override
	public void exportData(ScanRun scanRun) throws Exception {
		DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_DATE)
				.appendLiteral("T").appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral("-")
				.appendValue(ChronoField.MINUTE_OF_HOUR, 2).appendLiteral("-")
				.appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();

		String outputFilename = new StringBuilder().append("ppmInactiveManagers-")
				.append(scanRun.getScanTime().format(dateTimeFormatter)).append(".json").toString();

		LOGGER.info("Writing out the result to file: {}", outputFilename);

		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
				.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeJsonSerializer()).create();
		File outputJsonFile = new File(outputFilename);
		outputJsonFile.createNewFile();
		JsonWriter jsonWriter = gson.newJsonWriter(new FileWriterWithEncoding(outputJsonFile, "UTF-8"));
		gson.toJson(gson.toJsonTree(scanRun.getManagers()), jsonWriter);
		jsonWriter.close();
		LOGGER.info("Writing to the file was successful");

	}

	@Override
	public ScanRun importData() throws Exception {
		throw new Exception("Not supported");
	}

}
