package sk.ppmscan.application.processor;

import java.util.Set;

import sk.ppmscan.application.beans.Manager;
import sk.ppmscan.application.beans.Team;

public class ProcessedManager {
	
	private Manager manager;
	
	/**
	 * the manager should be ignored.
	 */
	private boolean ignorable;
	
	/**
	 * the manager fits the filters.
	 */
	private boolean filterable;
	
	/**
	 * the manager's teams fit the filters.
	 */
	private Set<Team> filterableTeams;

	public ProcessedManager(Manager manager) {
		super();
		this.manager = manager;
	}

	public Manager getManager() {
		return manager;
	}

	public void setManager(Manager manager) {
		this.manager = manager;
	}

	public boolean isIgnorable() {
		return ignorable;
	}

	public void setIgnorable(boolean ignorable) {
		this.ignorable = ignorable;
	}

	public boolean isFilterable() {
		return filterable;
	}

	public void setFilterable(boolean filterable) {
		this.filterable = filterable;
	}

	public Set<Team> getFilterableTeams() {
		return filterableTeams;
	}

	public void setFilterableTeams(Set<Team> filterableTeams) {
		this.filterableTeams = filterableTeams;
	}

}
