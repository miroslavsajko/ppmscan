package sk.ppmscan.application.processor;

import sk.ppmscan.application.beans.Manager;

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

}
