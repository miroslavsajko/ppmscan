package sk.ppmscan.application.config;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.ppmscan.application.PPMScannerApplication;
import sk.ppmscan.application.importexport.FilteredTeamsExporter;
import sk.ppmscan.application.importexport.excel.FilteredTeamsExcelExporter;
import sk.ppmscan.application.importexport.html.FilteredTeamsHtmlExporter;
import sk.ppmscan.application.importexport.json.FilteredTeamsJsonExporter;

public enum ExportFormat {
	
	JSON(FilteredTeamsJsonExporter.class), 
	EXCEL(FilteredTeamsExcelExporter.class),
	HTML(FilteredTeamsHtmlExporter.class);
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PPMScannerApplication.class);
	
	private Class<? extends FilteredTeamsExporter> filteredTeamsExporterClass;
	
	private ExportFormat(Class<? extends FilteredTeamsExporter> filteredTeamsExporterClass) {
		this.filteredTeamsExporterClass = filteredTeamsExporterClass;
	}
	
	public FilteredTeamsExporter getFilteredTeamsExporter(LocalDateTime now) {
		try {
			return filteredTeamsExporterClass.getConstructor(LocalDateTime.class).newInstance(now);
		} catch (Exception e) {
			LOGGER.error("Error occurred during a creation of the correct instance of FilteredTeamsExporter", e);
			LOGGER.info("Fallback to Html exporter");
			return new FilteredTeamsHtmlExporter(now);
		}
	}

}
