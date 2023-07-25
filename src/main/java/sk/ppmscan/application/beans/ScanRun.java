package sk.ppmscan.application.beans;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "scan_run")
public class ScanRun {

	public static final String COLUMN_NAME_SCAN_RUN_ID = "scan_run_id";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = COLUMN_NAME_SCAN_RUN_ID)
	private long scanRunId;

	@Column(name = "scan_time", nullable = false, unique = true)
	private LocalDateTime scanTime;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "scanRun", cascade = CascadeType.ALL)
	private List<Manager> managers;

	// TODO add configs used in this scan run

	public ScanRun() {
		super();
	}

	public ScanRun(long scanRunId, LocalDateTime scanTime) {
		super();
		this.scanRunId = scanRunId;
		this.scanTime = scanTime;
	}

	public long getScanRunId() {
		return scanRunId;
	}

	public void setScanRunId(long scanRunId) {
		this.scanRunId = scanRunId;
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
