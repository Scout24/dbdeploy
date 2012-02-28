package com.dbdeploy.database.changelog;

import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dbdeploy.scripts.ChangeScript;

public class DatabaseSchemaVersionManagerTest {
    private final ChangeScript script = new ChangeScript(99, "Some Description", "docontent99", "undoContent99");

    private DatabaseSchemaVersionManager schemaVersionManager;

    @Mock private ResultSet expectedResultSet;
    @Mock private QueryExecuter queryExecuter;
    @Mock private DatabaseSchemaVersionManager.CurrentTimeProvider timeProvider;

    @Before
    public void setUp() throws SQLException {
        MockitoAnnotations.initMocks(this);

        when(queryExecuter.executeQuery(anyString())).thenReturn(expectedResultSet);

        schemaVersionManager = new DatabaseSchemaVersionManager(queryExecuter, "changelog");
        schemaVersionManager.setTimeProvider(timeProvider);
    }

    @Test
    public void shouldUseQueryExecuterToReadChangeLogEntryIdsFromTheChangelogTable() throws Exception {
        when(expectedResultSet.next()).thenReturn(true, true, true, false);
        when(expectedResultSet.getLong(1)).thenReturn(5L, 9L, 12L);
        when(expectedResultSet.getTimestamp(2)).thenReturn(createTimestamp(), createTimestamp(), createTimestamp());
        when(expectedResultSet.getString(3)).thenReturn("userA", "userB", "userC");
        when(expectedResultSet.getString(4)).thenReturn("descriptionA", "descriptionB", "descriptionC");
        when(expectedResultSet.getString(5)).thenReturn("checksumA", "checksumA", "checksumA");

        final List<Long> ids = schemaVersionManager.findChangeLogEntryIds();
        assertThat(ids, hasItems(5L, 9L, 12L));
    }

    @Test
    public void shouldUseQueryExecuterToReadChangeLogEntriesFromTheChangelogTable() throws Exception {
    	when(expectedResultSet.next()).thenReturn(true, true, true, false);
    	when(expectedResultSet.getLong(1)).thenReturn(5L, 9L, 12L);
    	final Timestamp timestampC = createTimestamp();
		final Timestamp timestampB = createTimestamp();
		final Timestamp timestampA = createTimestamp();
		when(expectedResultSet.getTimestamp(2)).thenReturn(timestampA, timestampB, timestampC);
    	when(expectedResultSet.getString(3)).thenReturn("userA", "userB", "userC");
    	when(expectedResultSet.getString(4)).thenReturn("descriptionA", "descriptionB", "descriptionC");
    	when(expectedResultSet.getString(5)).thenReturn("checksumA", "checksumB", "checksumC");
    	
    	final List<ChangeLogEntry> changeLogEntries = schemaVersionManager.findChangeLogEntries();
    	
    	assertThat(changeLogEntries.get(0), isLike(new ChangeLogEntry(5L, timestampA, "userA", "descriptionA", "checksumA" )));
    	assertThat(changeLogEntries.get(1), isLike(new ChangeLogEntry(9L, timestampB, "userB", "descriptionB", "checksumB" )));
    	assertThat(changeLogEntries.get(2), isLike(new ChangeLogEntry(12L, timestampC, "userC", "descriptionC", "checksumC" )));
    }

	@Test
    public void shouldUpdateChangelogTable() throws Exception {
        Date now = new Date();

        when(queryExecuter.getDatabaseUsername()).thenReturn("DBUSER");
        when(timeProvider.now()).thenReturn(now);

        schemaVersionManager.recordScriptApplied(script);
        String expected =
                "INSERT INTO changelog (change_number, complete_dt, applied_by, description, checksum) " +
                        "VALUES (?, ?, ?, ?, ?)";

        verify(queryExecuter).execute(expected, script.getId(),
                new Timestamp(now.getTime()), "DBUSER", script.getDescription(), script.getChecksum());
    }

    @Test
    public void shouldGenerateSqlStringToDeleteChangelogTableAfterUndoScriptApplication() throws Exception {
        String sql = schemaVersionManager.getChangelogDeleteSql(script);
        String expected =
                "DELETE FROM changelog WHERE change_number = 99";
        assertThat(sql, equalToIgnoringWhiteSpace(expected));
    }

    @Test
    public void shouldGetAppliedChangesFromSpecifiedChangelogTableName() throws SQLException {
        DatabaseSchemaVersionManager schemaVersionManagerWithDifferentTableName =
                new DatabaseSchemaVersionManager(queryExecuter,
                        "user_specified_changelog");

        schemaVersionManagerWithDifferentTableName.findChangeLogEntryIds();

        verify(queryExecuter).executeQuery(startsWith("SELECT * FROM user_specified_changelog "));
    }

    @Test
    public void shouldGenerateSqlStringContainingSpecifiedChangelogTableNameOnDelete() {
        DatabaseSchemaVersionManager schemaVersionManagerWithDifferentTableName =
                new DatabaseSchemaVersionManager(queryExecuter,
                        "user_specified_changelog");

        String updateSql = schemaVersionManagerWithDifferentTableName.getChangelogDeleteSql(script);

        assertThat(updateSql, Matchers.startsWith("DELETE FROM user_specified_changelog "));
    }

    private Timestamp createTimestamp() {
    	return new Timestamp(new Date().getTime());
    }
    
    private Matcher<ChangeLogEntry> isLike(
			final ChangeLogEntry changeLogEntry) {
    	return new BaseMatcher<ChangeLogEntry>() {
			public boolean matches(Object object) {
				final ChangeLogEntry otherChangeLogEntry = (ChangeLogEntry) object;
				
				if (changeLogEntry.getId() != otherChangeLogEntry.getId()) {
					return false;
				}
				
				if (!changeLogEntry.getTimestamp().equals(otherChangeLogEntry.getTimestamp())) {
					return false;
				}
				
				if (!changeLogEntry.getUserName().equals(otherChangeLogEntry.getUserName())) {
					return false;
				}
				
				if (!changeLogEntry.getDescription().equals(otherChangeLogEntry.getDescription())) {
					return false;
				}
				
				if (!changeLogEntry.getChecksum().equals(otherChangeLogEntry.getChecksum())) {
					return false;
				}
				return true;
			}

			public void describeTo(Description description) {
				description.appendText(" a change log entry like ").appendValue(changeLogEntry);
			}
		};
	}
}

