package sk.ppmscan.application.config;

import sk.ppmscan.application.importexport.FilteredTeamsExporter;
import sk.ppmscan.application.importexport.excel.FilteredTeamsExcelExporter;
import sk.ppmscan.application.importexport.hibernate.FilteredManagersHibernateExporter;
import sk.ppmscan.application.importexport.html.FilteredTeamsHtmlExporter;
import sk.ppmscan.application.importexport.json.FilteredTeamsJsonExporter;

public enum ExportFormat {
	
	JSON(new FilteredTeamsJsonExporter()), 
	EXCEL(new FilteredTeamsExcelExporter()),
	HTML(new FilteredTeamsHtmlExporter()),
	HIBERNATE(new FilteredManagersHibernateExporter());
	
	private FilteredTeamsExporter filteredTeamsExporter;
	
	private ExportFormat(FilteredTeamsExporter filteredTeamsExporter) {
		this.filteredTeamsExporter = filteredTeamsExporter;
	}
	
	public FilteredTeamsExporter getFilteredTeamsExporter() {
		return this.filteredTeamsExporter;
	}

}
