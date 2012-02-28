package com.dbdeploy.scripts;

import static com.dbdeploy.ConstraintUtils.ensureGreaterThanZero;
import static com.dbdeploy.ConstraintUtils.ensureNotNull;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.dbdeploy.exceptions.DbDeployException;

/*
 * (michael)
 * During a refactoring this has become a tuple (id, description, doContent, undoContent)
 * It is a tuple to ensure that the file contents fit to the checksum.
 * 
 * Since most of the change scripts have already been executed,
 * a lazy loading mechanism for content and checksum is more economical.
 * But still we would have to ensure, that the checksum has not changed when executing the script 
 * (which is after checksum-validation).
 * 
 * @author Graham Tackley
 * @author Michael Gruber
 */

public class ChangeScript implements Comparable<ChangeScript> {
	private static final String UNDO_MARKER = "--//@UNDO";

	private final long id;
	private final String checksum;
	private final String doContent;
	private final String undoContent;
	private final String description;

    public ChangeScript(final long id, final String description, final String doContent, final String undoContent) {
    	this.id = ensureGreaterThanZero("id", id);
    	this.description = ensureNotNull("description", description);
    	this.doContent = ensureNotNull("doContent", doContent);
    	this.undoContent = ensureNotNull("encoding", undoContent);
    	this.checksum = sha256Hex(doContent + undoContent);
    }
    
	public ChangeScript(final long id, final File file, final String encoding) {
		this(id, ensureNotNull("file", file).getName(), getFileContents(file, ensureNotNull("encoding", encoding), false), getFileContents(file, encoding, true));
	}
	
	public long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public int compareTo(ChangeScript other) {
		return Long.valueOf(this.id).compareTo(other.id);
	}

	@Override
	public String toString() {
		return "#" + id + ": " + description;
	}

	public String getContent() {
		return doContent;
	}

	public String getUndoContent() {
		return undoContent;
	}

	private static String getFileContents(final File file, final String encoding, final boolean onlyAfterUndoMarker) {
		
		try {
			final StringBuilder content = new StringBuilder();
			boolean foundUndoMarker = false;
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));

			try {
				for (;;) {
					String str = reader.readLine();

					if (str == null)
						break;

					if (str.trim().equals(UNDO_MARKER)) {
						foundUndoMarker = true;
						continue;
					}

					if (foundUndoMarker == onlyAfterUndoMarker) {
						content.append(str);
						content.append('\n');
					}
				}
			} finally {
				reader.close();
			}

			return content.toString();
		} catch (IOException e) {
			throw new DbDeployException("Failed to read change script file", e);
		}
	}

	public String getChecksum()  {
		return checksum;
		
	}
}
