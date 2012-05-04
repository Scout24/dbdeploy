package com.dbdeploy.exceptions;

import com.dbdeploy.scripts.Script;
import java.sql.SQLException;


public class ScriptFailedException extends DbDeployException {
	private final Script script;
	private final int statement;
	private final String executedSql;

	public ScriptFailedException(SQLException cause, Script script,
								 int statement, String executedSql) {
		super(cause);
		this.script = script;
		this.statement = statement;
		this.executedSql = executedSql;
	}

	public Script getScript() {
		return script;
	}

	public String getExecutedSql() {
		return executedSql;
	}

	public int getStatement() {
		return statement;
	}

	@Override
	public String getMessage() {
		return "change script " + script +
			" failed while executing statement " + statement + ":\n" +
			executedSql + "\n -> " + getCause().getMessage();
	}
}
