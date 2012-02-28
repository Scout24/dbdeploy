package com.dbdeploy;

import java.util.List;

import com.dbdeploy.scripts.ChangeScript;

public class ChecksumValidationException extends IllegalStateException {
	private static final long serialVersionUID = 1L;

	public ChecksumValidationException( final List<ChangeScript> modifiedScripts){ 
		super("the following scripts have a modified checksum:\n" + formatListOfChangeScripts(modifiedScripts));
	}
	
	private static String formatListOfChangeScripts(final List<ChangeScript> scripts) {
		final StringBuffer result = new StringBuffer();
		
		for (final ChangeScript script: scripts) {
			result.append(script);
			result.append("\n");
		}
		
		return result.toString();
	}
}
