package sk.ppmscan.application.beans;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "ignored_managers", indexes = {
		@Index(columnList = "manager_id", name = "idx_ignored_managers", unique = true) })
public class IgnoredManager {

	/**
	 * Id of the row in the database. Always unique.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ignored_manager_pk", insertable = false, updatable = false)
	private long id;

	@Column(name = "manager_id", nullable = false, unique = true)
	private Long managerId;

	public IgnoredManager() {
		super();
	}

	public IgnoredManager(Long managerId) {
		super();
		this.managerId = managerId;
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

}
