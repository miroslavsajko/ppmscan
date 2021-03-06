package sk.ppmscan.application.importexport.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.ppmscan.application.importexport.IgnoredManagersImportExport;

public class IgnoredManagersSQliteImportExport implements IgnoredManagersImportExport {

	private static final Logger LOGGER = LoggerFactory.getLogger(IgnoredManagersSQliteImportExport.class);

	public Connection getConnection() throws SQLException {
		String url = "jdbc:sqlite:ppmScan.db";

		Connection conn = DriverManager.getConnection(url);
		String sql = "CREATE TABLE IF NOT EXISTS ignoredManagers (id integer PRIMARY KEY, managerId integer UNIQUE);";
		Statement stmt = conn.createStatement();
		stmt.execute(sql);
		return conn;
	}

	public void insert(Set<Long> managerIds) throws SQLException {
		
		if (managerIds.isEmpty()) {
			LOGGER.info("There aren't any manager ids to insert");
			return;
		}

		Connection connection = this.getConnection();

		PreparedStatement statement = connection
				.prepareStatement("INSERT OR IGNORE INTO ignoredManagers (managerId) VALUES (?)");
		int count = 0;
		int inserted = 0;
		for (Long managerId : managerIds) {
			statement.setLong(1, managerId);
			statement.addBatch();

			if (++count % 1000 == 0) {
				int[] executed = statement.executeBatch();
				int sum = Arrays.stream(executed).filter(a->a>=0).sum();
				inserted += sum;
				LOGGER.info("Executed batch {}, inserted {}", count, sum);
			}
		}

		int[] executed = statement.executeBatch();
		int sum = Arrays.stream(executed).filter(a->a>=0).sum();
		inserted += sum;
		LOGGER.info("Executed batch {}, inserted {}", count, sum);
		connection.close();
		LOGGER.info("Inserted {} rows", inserted);
		
	}

	public Set<Long> selectAll() throws SQLException {

		String sql = "SELECT * FROM ignoredManagers";

		Connection conn = this.getConnection();
		Statement stmt = conn.createStatement();
		ResultSet resultSet = stmt.executeQuery(sql);

		Set<Long> ignoredManagers = new TreeSet<>();

		while (resultSet.next()) {
			ignoredManagers.add(resultSet.getLong("managerId"));
		}

		LOGGER.info("Select all returned {} managers ids", ignoredManagers.size());
		conn.close();
		return ignoredManagers;
	}

	@Override
	public Set<Long> importData() throws Exception {
		LOGGER.info("Importing ignored managers from sqlite");
		long startTime = System.currentTimeMillis();
		Set<Long> data = this.selectAll();
		LOGGER.info("The operation took {} ms", System.currentTimeMillis() - startTime);
		return data;
	}

	@Override
	public void exportData(Set<Long> data) throws Exception {
		LOGGER.info("Exporting ignored managers to sqlite");
		long startTime = System.currentTimeMillis();
		Set<Long> existingData = this.selectAll();
		data.removeAll(existingData);
		this.insert(data);
		LOGGER.info("The operation took {} ms", System.currentTimeMillis() - startTime);
	}

}
