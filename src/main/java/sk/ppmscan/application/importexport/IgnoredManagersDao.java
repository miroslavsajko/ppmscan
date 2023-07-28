package sk.ppmscan.application.importexport;

import java.util.Set;

public interface IgnoredManagersDao {
	
	public Set<Long> importData() throws Exception;
	
	public void exportData(Set<Long> data) throws Exception;

}
