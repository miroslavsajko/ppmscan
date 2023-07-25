package sk.ppmscan.application.beans;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "manager", uniqueConstraints = @UniqueConstraint(columnNames = { Manager.COLUMN_NAME_MANAGER_ID, ScanRun.COLUMN_NAME_SCAN_RUN_ID }))
public class Manager {
	
	public static final String COLUMN_NAME_MANAGER_ID = "manager_id";

	/**
	 * Id of the row in the database. Always unique.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "manager_pk", insertable = false, updatable = false)
	private long id;

	@Column(name = COLUMN_NAME_MANAGER_ID, nullable = false)
	private Long managerId;

	@ManyToOne
	@JoinColumn(name = ScanRun.COLUMN_NAME_SCAN_RUN_ID)
	private ScanRun scanRun;

	/**
	 * True if the user is blocked. Transient means it won't be serialized.
	 */
	@Transient
	private transient boolean blocked;

	@Column(name = "url", nullable = false)
	private String url;

	@Column(name = "nickname", nullable = false)
	private String nickname;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "manager", cascade = CascadeType.ALL)
	private List<Team> teams;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "manager_recent_logins")
	private List<LocalDateTime> recentLogins;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public List<Team> getTeams() {
		return teams;
	}

	public void setTeams(List<Team> teams) {
		this.teams = teams;
	}

	public List<LocalDateTime> getRecentLogins() {
		return recentLogins;
	}

	public void setRecentLogins(List<LocalDateTime> recentLogins) {
		this.recentLogins = recentLogins;
	}

	public boolean isBlocked() {
		return blocked;
	}

	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Long getManagerId() {
		return managerId;
	}

	public void setManagerId(Long managerId) {
		this.managerId = managerId;
	}

	public ScanRun getScanRun() {
		return scanRun;
	}

	public void setScanRun(ScanRun scanRun) {
		this.scanRun = scanRun;
	}

}
