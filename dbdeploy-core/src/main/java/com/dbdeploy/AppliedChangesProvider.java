package com.dbdeploy;

import java.util.List;

import com.dbdeploy.database.changelog.ChangeLogEntry;

public interface AppliedChangesProvider {
	List<Long> findChangeLogEntryIds();
	
	List<ChangeLogEntry> findChangeLogEntries();
}
