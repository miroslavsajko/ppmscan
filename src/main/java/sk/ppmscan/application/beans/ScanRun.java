package sk.ppmscan.application.beans;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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

	@Override
	public int hashCode() {
		return Objects.hash(managers, scanTime);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScanRun other = (ScanRun) obj;
		return Objects.equals(managers, other.managers) && Objects.equals(scanTime, other.scanTime);
	}

}
