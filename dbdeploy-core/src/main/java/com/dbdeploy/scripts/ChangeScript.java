package com.dbdeploy.scripts;

import static com.dbdeploy.ConstraintUtils.ensureGreaterThanZero;
import static com.dbdeploy.ConstraintUtils.ensureNotNull;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import java.io.File;


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

public class ChangeScript extends Script implements Comparable<ChangeScript> {
	private final long id;
	private final String checksum;
	private final String undoContent;

	public ChangeScript(final long id, final String description, final String doContent, final String undoContent) {
		super(description, doContent);
		this.id = ensureGreaterThanZero("id", id);
		this.undoContent = ensureNotNull("encoding", undoContent);
		this.checksum = sha256Hex(doContent + undoContent);
	}

	public ChangeScript(final long id, final File file, final String encoding) {
		this(id, ensureNotNull("file", file).getName(),
			getFileContents(file, ensureNotNull("encoding", encoding), false),
			getFileContents(file, encoding, true));
	}

	public long getId() {
		return id;
	}

	public int compareTo(ChangeScript other) {
		return Long.valueOf(this.id).compareTo(other.id);
	}

	@Override
	public String toString() {
		return "#" + id + ": " + description;
	}

	public String getUndoContent() {
		return undoContent;
	}

	public String getChecksum() {
		return checksum;

	}
}
