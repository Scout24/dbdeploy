package com.dbdeploy;


import java.sql.Timestamp;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.dbdeploy.database.changelog.ChangeLogEntry;

public class ChangeLogEntryTest {
	private Timestamp timestamp;
	
	@Before
	public void initializeTimestamp() {
		timestamp = new Timestamp(new Date().getTime());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenGivenIdIsSmallerThanZero() {
		new ChangeLogEntry(-1, timestamp, "userName", "description", "checksum");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenGivenTimestampIsNull() {
		new ChangeLogEntry(0, null, "userName", "description", "checksum");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenGivenUserNameIsNull() {
		new ChangeLogEntry(1, timestamp, null, "description", "checksum");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenGivenDescriptionIsNull() {
		new ChangeLogEntry(2, timestamp, "userName", null, "checksum");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenGivenChecksumIsNull() {
		new ChangeLogEntry(3, timestamp, "userName", "description", null);
	}
	
}
