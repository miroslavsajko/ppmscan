package sk.ppmscan.application.export;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import sk.ppmscan.application.beans.Sport;
import sk.ppmscan.application.beans.Team;

public interface IExporter {
	
	public void export(Map<Sport, Set<Team>> teams) throws IOException;

}
