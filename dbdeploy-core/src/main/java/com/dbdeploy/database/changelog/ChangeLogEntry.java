package com.dbdeploy.database.changelog;

import static com.dbdeploy.ConstraintUtils.ensureGreaterThanZero;
import static com.dbdeploy.ConstraintUtils.ensureNotNull;

import java.sql.Timestamp;

public class ChangeLogEntry {
	private final long id;
	private final Timestamp timestamp;
	private final String userName;
	private final String description;
	private final String checksum;
	
	public ChangeLogEntry(final long id, final Timestamp timstamp, final String userName, final String description, final String checksum ) {
		this.id = ensureGreaterThanZero("id", id);
		this.timestamp = ensureNotNull("timestamp", timstamp);
		this.userName = ensureNotNull("userName", userName);
		this.description = ensureNotNull("description", description);
		this.checksum = ensureNotNull("checksum", checksum);
	}

	public long getId() {
		return id;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public String getUserName() {
		return userName;
	}

	public String getDescription() {
		return description;
	}

	public String getChecksum() {
		return checksum;
	}
	
	public String toString() {
		return ChangeLogEntry.class.getSimpleName() 
				+ " { id: " + id 
				+ ", timestamp: '" + timestamp 
				+ "', userName: '" + userName
				+ "', description: '" + description
				+ "', checksum: '" + checksum + "'}";
	}
}
