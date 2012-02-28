package com.dbdeploy.integration;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.dbdeploy.DbDeploy;
import com.dbdeploy.database.changelog.ChangeLogEntry;
import com.dbdeploy.database.changelog.DatabaseSchemaVersionManager;
import com.dbdeploy.database.changelog.QueryExecuter;
import com.dbdeploy.exceptions.SchemaVersionTrackingException;

public class Database {
	String connectionString;
	Connection connection;
    private final String changeLogTableName;

	private static final String DATABASE_SYNTAX = "hsql";
	private static final String DATABASE_DRIVER = "org.hsqldb.jdbcDriver";
	private static final String DATABASE_USERNAME = "sa";
	private static final String DATABASE_PASSWORD = "";

    public Database(String databaseName) throws ClassNotFoundException, SQLException {
        this(databaseName, "changelog");
    }

    public Database(String databaseName, String changeLogTableName) throws ClassNotFoundException, SQLException {
        this.changeLogTableName = changeLogTableName;
        connectionString = "jdbc:hsqldb:mem:" + databaseName;
		connection = openConnection();
	}

	private Connection openConnection() throws ClassNotFoundException, SQLException {
		Class.forName(DATABASE_DRIVER);
		return DriverManager.getConnection(connectionString, DATABASE_USERNAME, DATABASE_PASSWORD);
	}

	public void createSchemaVersionTable() throws SQLException {
		execute("CREATE TABLE " + changeLogTableName +
                " ( " +
				"  change_number INTEGER NOT NULL, " +
				"  complete_dt TIMESTAMP NOT NULL, " +
				"  applied_by VARCHAR(100) NOT NULL, " +
				"  description VARCHAR(500) NOT NULL, " +
				"  checksum VARCHAR(100)" +
				")");

		execute("ALTER TABLE " + changeLogTableName +
                " ADD CONSTRAINT Pkchangelog PRIMARY KEY (change_number)");
	}

	private void execute(String sql) throws SQLException {
		final Statement statement = connection.createStatement();
		statement.execute(sql);
		statement.close();
	}

	public void applyDatabaseSettingsTo(DbDeploy dbDeploy) {
		dbDeploy.setDbms(DATABASE_SYNTAX);
		dbDeploy.setDriver(DATABASE_DRIVER);
		dbDeploy.setUrl(connectionString);
		dbDeploy.setUserid(DATABASE_USERNAME);
		dbDeploy.setPassword(DATABASE_PASSWORD);
	}

	public void applyScript(File sqlFile) throws SQLException, IOException {
		String sql = FileUtils.readFileToString(sqlFile);

		final String[] statements = sql.split(";");

		for (String statement : statements) {
			execute(statement);
		}
	}

	public List<Object[]> executeQuery(String sql) throws SQLException {
		final Statement statement = connection.createStatement();
		final ResultSet rs = statement.executeQuery(sql);


		List<Object[]> results = new ArrayList<Object[]>();

		ResultSetMetaData meta = rs.getMetaData();
		int colmax = meta.getColumnCount();

		for (; rs.next();) {
			Object[] thisRow = new Object[colmax];
			for (int i = 0; i < colmax; ++i) {
				thisRow[i] = rs.getObject(i + 1);
			}

			results.add(thisRow);
		}

		statement.close();

		return results;
	}
	
	public List<Long> findChangeLogEntryIds() throws SchemaVersionTrackingException, SQLException {
		final List<ChangeLogEntry> changeLogEntries = getChangelogEntries();
		final List<Long> ids = new ArrayList<Long>();
		
		for (final ChangeLogEntry changeLogEntry: changeLogEntries) {
			ids.add(changeLogEntry.getId());
		}
		
		return ids;
	}

	public List<ChangeLogEntry> getChangelogEntries() throws SchemaVersionTrackingException, SQLException {
		final QueryExecuter queryExecuter = new QueryExecuter(connectionString, DATABASE_USERNAME, DATABASE_PASSWORD);

		final DatabaseSchemaVersionManager schemaVersionManager =
                new DatabaseSchemaVersionManager(queryExecuter, changeLogTableName);
		return schemaVersionManager.findChangeLogEntries();
	}
}
