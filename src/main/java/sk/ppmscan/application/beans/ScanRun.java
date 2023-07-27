package sk.ppmscan.application.beans;

import java.time.LocalDateTime;
import java.util.List;

public class ScanRun {

	public static final String COLUMN_NAME_SCAN_RUN_ID = "scan_run_id";

	private LocalDateTime scanTime;

	private List<Manager> managers;

	// TODO add configs used in this scan run

	public ScanRun() {
		super();
	}

	public ScanRun(LocalDateTime scanTime) {
		super();
		this.scanTime = scanTime;
	}

	public LocalDateTime getScanTime() {
		return scanTime;
	}

	public void setScanTime(LocalDateTime scanTime) {
		this.scanTime = scanTime;
	}

	public List<Manager> getManagers() {
		return managers;
	}

	public void setManagers(List<Manager> managers) {
		this.managers = managers;
	}

}
