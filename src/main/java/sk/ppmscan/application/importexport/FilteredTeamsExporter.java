package sk.ppmscan.application.importexport;

import java.util.Map;
import java.util.Set;

import sk.ppmscan.application.beans.Sport;
import sk.ppmscan.application.beans.Team;

public interface FilteredTeamsExporter extends ImportExportInterface<Map<Sport, Set<Team>>> {

}
