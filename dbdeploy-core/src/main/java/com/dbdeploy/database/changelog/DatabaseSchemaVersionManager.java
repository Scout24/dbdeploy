package com.dbdeploy.database.changelog;

import com.dbdeploy.AppliedChangesProvider;
import com.dbdeploy.exceptions.SchemaVersionTrackingException;
import com.dbdeploy.scripts.ChangeScript;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class is responsible for all interaction with the changelog table
 */
public class DatabaseSchemaVersionManager implements AppliedChangesProvider {

    private final QueryExecuter queryExecuter;
    private final String changeLogTableName;
    private CurrentTimeProvider timeProvider = new CurrentTimeProvider();

    public DatabaseSchemaVersionManager(QueryExecuter queryExecuter, String changeLogTableName) {
        this.queryExecuter = queryExecuter;
        this.changeLogTableName = changeLogTableName;
    }
    
	public List<Long> findChangeLogEntryIds() {
		final List<ChangeLogEntry> changeLogEntries = findChangeLogEntries();
		final List<Long> ids = new ArrayList<Long>();
		
		for (final ChangeLogEntry changeLogEntry: changeLogEntries) {
			ids.add(changeLogEntry.getId());
		}
		
		return ids;
	}

	public List<ChangeLogEntry> findChangeLogEntries() {
		try {
			ResultSet rs = queryExecuter.executeQuery(
					"SELECT * FROM " + changeLogTableName + "  ORDER BY change_number");
			
			List<ChangeLogEntry> changeLogsEntries = new ArrayList<ChangeLogEntry>();
			
			while (rs.next()) {
				final ChangeLogEntry changeLogEntry = new ChangeLogEntry(
						rs.getLong(1), rs.getTimestamp(2), rs.getString(3), rs.getString(4), rs.getString(5));
				changeLogsEntries.add(changeLogEntry);
			}
			
			rs.close();
			
			return changeLogsEntries;
		} catch (SQLException e) {
			throw new SchemaVersionTrackingException("Could not retrieve change log entry from database because: "
					+ e.getMessage(), e);
		}
	}
	
    public String getChangelogDeleteSql(ChangeScript script) {
		return String.format(
			"DELETE FROM " + changeLogTableName + " WHERE change_number = %d",
				script.getId());
	}

    public void recordScriptApplied(ChangeScript script) {
        try {
            queryExecuter.execute(
                    "INSERT INTO " + changeLogTableName + " (change_number, complete_dt, applied_by, description, checksum)" +
                            " VALUES (?, ?, ?, ?, ?)",
                    script.getId(),
                    new Timestamp(timeProvider.now().getTime()),
                    queryExecuter.getDatabaseUsername(),
                    script.getDescription(),
                    script.getChecksum()
                    );
        } catch (final SQLException e) {
            throw new SchemaVersionTrackingException("Could not update change log because: "
                    + e.getMessage(), e);
        }
    }

    public void setTimeProvider(final CurrentTimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public static class CurrentTimeProvider {
        public Date now() {
            return new Date();
        }
    }

}
