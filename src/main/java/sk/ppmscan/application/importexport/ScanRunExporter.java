package sk.ppmscan.application.importexport;

import sk.ppmscan.application.beans.ScanRun;

public interface ScanRunExporter   {

	public void exportData(ScanRun data) throws Exception;

}
