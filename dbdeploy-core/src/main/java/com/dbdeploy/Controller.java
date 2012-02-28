package com.dbdeploy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dbdeploy.database.changelog.ChangeLogEntry;
import com.dbdeploy.exceptions.DbDeployException;
import com.dbdeploy.scripts.ChangeScript;

public class Controller {

	private final AvailableChangeScriptsProvider availableChangeScriptsProvider;
	private final AppliedChangesProvider appliedChangesProvider;
	private final ChangeScriptApplier changeScriptApplier;
	private final ChangeScriptApplier undoScriptApplier;

	private final PrettyPrinter prettyPrinter = new PrettyPrinter();

	public Controller(AvailableChangeScriptsProvider availableChangeScriptsProvider,
					  AppliedChangesProvider appliedChangesProvider,
					  ChangeScriptApplier changeScriptApplier, ChangeScriptApplier undoScriptApplier) {
		this.availableChangeScriptsProvider = availableChangeScriptsProvider;
		this.appliedChangesProvider = appliedChangesProvider;
		this.changeScriptApplier = changeScriptApplier;
		this.undoScriptApplier = undoScriptApplier;
	}

	public void processChangeScripts(Long lastChangeToApply) throws DbDeployException, IOException {
		if (lastChangeToApply != Long.MAX_VALUE) {
			info("Only applying changes up and including change script #" + lastChangeToApply);
		}
		
		final List<ChangeScript> scripts = availableChangeScriptsProvider.getAvailableChangeScripts();
		final List<Long> applied = appliedChangesProvider.findChangeLogEntryIds();
		
		final List<ChangeLogEntry> changeLogEntries = appliedChangesProvider.findChangeLogEntries();
		validateChecksums(scripts, changeLogEntries);	
		
		final List<ChangeScript> toApply = identifyChangesToApply(lastChangeToApply, scripts, applied);

		logStatus(scripts, applied, toApply);

        changeScriptApplier.apply(Collections.unmodifiableList(toApply));

        if (undoScriptApplier != null) {
			info("Generating undo scripts...");
			Collections.reverse(toApply);
            undoScriptApplier.apply(Collections.unmodifiableList(toApply));
        }
	}

	private void validateChecksums(final List<ChangeScript> scripts, List<ChangeLogEntry> changeLogEntries) {
		System.out.println("Validating checksums ... ");
		final List<ChangeScript> modifiedChangeScripts = new ArrayList<ChangeScript>();
		
		for (final ChangeScript script: scripts) {
			for (final ChangeLogEntry entry: changeLogEntries) {
				if (script.getId() == entry.getId()) {
					if (!script.getChecksum().equals(entry.getChecksum())) {
						info("Invalid checksum for script " + script + ".");
						modifiedChangeScripts.add(script);
					}
					
					continue;
				}
			}
		}
		
		if (!modifiedChangeScripts.isEmpty()) {
			throw new ChecksumValidationException(modifiedChangeScripts);
		}
	}

	private void logStatus(List<ChangeScript> scripts, List<Long> applied, List<ChangeScript> toApply) {
		info("Changes currently applied to database:\n  " + prettyPrinter.format(applied));
		info("Scripts available:\n  " + prettyPrinter.formatChangeScriptList(scripts));
		info("To be applied:\n  " + prettyPrinter.formatChangeScriptList(toApply));
	}

	private List<ChangeScript> identifyChangesToApply(Long lastChangeToApply, List<ChangeScript> scripts, List<Long> applied) {
		List<ChangeScript> result = new ArrayList<ChangeScript>();

		for (ChangeScript script : scripts) {
			if (script.getId() > lastChangeToApply)
				break;

			if (!applied.contains(script.getId())) {
				result.add(script);
			}
		}

		return result;
	}

	private void info(String string) {
		System.err.println(string);
	}
}