package com.dbdeploy.appliers;

import com.dbdeploy.ChangeScriptApplier;
import com.dbdeploy.database.QueryStatementSplitter;
import com.dbdeploy.database.changelog.DatabaseSchemaVersionManager;
import com.dbdeploy.database.changelog.QueryExecuter;
import com.dbdeploy.exceptions.ScriptFailedException;
import com.dbdeploy.scripts.ChangeScript;
import com.dbdeploy.scripts.Script;
import java.sql.SQLException;
import java.util.List;


public class DirectToDbApplier implements ChangeScriptApplier {
	private final QueryExecuter queryExecuter;
	private final DatabaseSchemaVersionManager schemaVersionManager;
	private final QueryStatementSplitter splitter;
	private Script preScriptExecutionScript;
	private Script postScriptExecutionScript;

	public DirectToDbApplier(QueryExecuter queryExecuter, DatabaseSchemaVersionManager schemaVersionManager,
							 QueryStatementSplitter splitter) {
		this.queryExecuter = queryExecuter;
		this.schemaVersionManager = schemaVersionManager;
		this.splitter = splitter;
	}

	public void apply(List<ChangeScript> changeScript) {
		begin();

		for (ChangeScript script : changeScript) {
			System.err.println("Applying " + script + "...");

			applyPreScriptScript();

			applyChangeScript(script);

			applyPostScriptScript();
			insertToSchemaVersionTable(script);

			commitTransaction();
		}
	}

	private void applyPreScriptScript() {
		if (preScriptExecutionScript != null) {
			System.err.println("Applying pre script " + preScriptExecutionScript + "...");
			applyScript(preScriptExecutionScript);
		}
	}

	private void applyPostScriptScript() {
		if (postScriptExecutionScript != null) {
			System.err.println("Applying post script " + postScriptExecutionScript + "...");
			applyScript(postScriptExecutionScript);
		}
	}


	public void begin() {
		try {
			queryExecuter.setAutoCommit(false);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	protected void applyChangeScript(ChangeScript script) {
		applyScript(script);
	}

	protected void applyScript(Script script) {
		List<String> statements = splitter.split(script.getContent());

		for (int i = 0; i < statements.size(); i++) {
			String statement = statements.get(i);
			try {
				if (statements.size() > 1) {
					System.err.println(" -> statement " + (i + 1) + " of " + statements.size() + "...");
				}
				queryExecuter.execute(statement);
			} catch (SQLException e) {
				throw new ScriptFailedException(e, script, i + 1, statement);
			}
		}
	}


	protected void insertToSchemaVersionTable(ChangeScript changeScript) {
		schemaVersionManager.recordScriptApplied(changeScript);
	}

	protected void commitTransaction() {
		try {
			queryExecuter.commit();
		} catch (SQLException e) {
			throw new RuntimeException();
		}
	}

	public Script getPreScriptExecutionScript() {
		return preScriptExecutionScript;
	}

	public void setPreScriptExecutionScript(Script preScriptExecutionScript) {
		this.preScriptExecutionScript = preScriptExecutionScript;
	}

	public Script getPostScriptExecutionScript() {
		return postScriptExecutionScript;
	}

	public void setPostScriptExecutionScript(Script postScriptExecutionScript) {
		this.postScriptExecutionScript = postScriptExecutionScript;
	}
}
