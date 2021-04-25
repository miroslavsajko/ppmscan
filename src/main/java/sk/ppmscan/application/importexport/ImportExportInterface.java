package sk.ppmscan.application.importexport;

public interface ImportExportInterface<T> {

	public T importData() throws Exception;
	
	public void exportData(T data) throws Exception;
	
}
