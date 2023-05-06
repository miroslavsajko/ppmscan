package sk.ppmscan.application.config;

import sk.ppmscan.application.importexport.IgnoredManagersImportExport;
import sk.ppmscan.application.importexport.json.IgnoredManagersJsonImportExport;
import sk.ppmscan.application.importexport.sqlite.IgnoredManagersSQliteImportExport;

public enum IgnoredManagersFormat {

	JSON(new IgnoredManagersJsonImportExport()),
	
	SQLITE(new IgnoredManagersSQliteImportExport());

	private IgnoredManagersImportExport ignoredManagersImportExporter;
	
	private IgnoredManagersFormat(IgnoredManagersImportExport ignoredManagersImportExporter) {
		this.ignoredManagersImportExporter = ignoredManagersImportExporter;
	}

	public IgnoredManagersImportExport getIgnoredManagersImportExporter() {
		return ignoredManagersImportExporter;
	}

}
