package com.dbdeploy.scripts;

import com.dbdeploy.exceptions.DbDeployException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import static com.dbdeploy.ConstraintUtils.ensureNotNull;


/**
 * extracted superclass from ChangeScript
 */
public class Script {
	private static final String UNDO_MARKER = "--//@UNDO";

	protected final String doContent;
	protected final String description;


	public Script(final String description, final String doContent) {
		this.description = ensureNotNull("description", description);
		this.doContent = ensureNotNull("doContent", doContent);
	}

	public Script(final File file, final String encoding) {
		this(ensureNotNull("file", file).getName(), getFileContents(file, ensureNotNull("encoding", encoding), false));
	}

	@Override
	public String toString() {
		return "script : " + description;
	}


	public String getDescription() {
		return description;
	}

	public String getContent() {
		return doContent;
	}

	protected static String getFileContents(final File file, final String encoding, final boolean onlyAfterUndoMarker) {
		try {
			final StringBuilder content = new StringBuilder();
			boolean foundUndoMarker = false;
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));

			try {
				for (;;) {
					String str = reader.readLine();

					if (str == null) {
						break;
					}

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

}
