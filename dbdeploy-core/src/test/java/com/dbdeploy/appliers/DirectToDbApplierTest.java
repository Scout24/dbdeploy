package com.dbdeploy.appliers;

import com.dbdeploy.database.QueryStatementSplitter;
import com.dbdeploy.database.changelog.DatabaseSchemaVersionManager;
import com.dbdeploy.database.changelog.QueryExecuter;
import com.dbdeploy.exceptions.ScriptFailedException;
import com.dbdeploy.scripts.ChangeScript;
import com.dbdeploy.scripts.Script;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnit44Runner;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnit44Runner.class)
public class DirectToDbApplierTest {
	@Mock
	private QueryExecuter queryExecuter;
	@Mock
	private DatabaseSchemaVersionManager schemaVersionManager;
	@Mock
	private QueryStatementSplitter splitter;
	private DirectToDbApplier applier;

	@Before
	public void setUp() {
		applier = new DirectToDbApplier(queryExecuter, schemaVersionManager, splitter);
	}

	@Test
	public void shouldSetConnectionToManualCommitModeAtStart() throws Exception {
		applier.begin();

		verify(queryExecuter).setAutoCommit(false);
	}

	@Test
	public void shouldApplyChangeScriptBySplittingContentUsingTheSplitter() throws Exception {
		when(splitter.split("split; content")).thenReturn(Arrays.asList("split", "content"));

		applier.applyChangeScript(new ChangeScript(1, "script", "split; content", "undoContent1"));

		checkExecutionOrder("split", "content");
	}

	@Test
	public void shouldApplyPreScript() throws Exception {
		when(splitter.split("prescript")).thenReturn(Arrays.asList("prescript"));
		when(splitter.split("content")).thenReturn(Arrays.asList("content"));
		applier.setPreScriptExecutionScript(new Script("preScript", "prescript"));

		applier.apply(getSimpleChangeScripts());

		checkExecutionOrder("prescript", "content");
	}


	@Test
	public void shouldApplyPostScript() throws Exception {
		when(splitter.split("content")).thenReturn(Arrays.asList("content"));
		when(splitter.split("postscript")).thenReturn(Arrays.asList("postscript"));
		applier.setPostScriptExecutionScript(new Script("postScript", "postscript"));

		applier.apply(getSimpleChangeScripts());

		checkExecutionOrder("content", "postscript");
	}

	private ArrayList<ChangeScript> getSimpleChangeScripts() {
		ArrayList<ChangeScript> changeScripts = new ArrayList<ChangeScript>();
		changeScripts.add(new ChangeScript(1, "script", "content", "undoContent1"));
		return changeScripts;
	}

	private void checkExecutionOrder(String... statements) throws SQLException {
		InOrder inOrder = inOrder(queryExecuter);
		for (int i = 0; i < statements.length; i++) {
			inOrder.verify(queryExecuter).execute(statements[i]);
		}
	}

	@Test
	public void shouldRethrowSqlExceptionsWithInformationAboutWhatStringFailed() throws Exception {
		when(splitter.split("split; content")).thenReturn(Arrays.asList("split", "content"));

		Script script = new ChangeScript(1, "script", "split; content", "undoContent1");

		doThrow(new SQLException("dummy exception")).when(queryExecuter).execute("split");

		try {
			applier.applyScript(script);
			fail("exception expected");
		} catch (ScriptFailedException e) {
			assertThat(e.getExecutedSql(), is("split"));
			assertThat(e.getScript(), is(script));
		}

		verify(queryExecuter, never()).execute("content");
	}

	@Test
	public void shouldInsertToSchemaVersionTable() throws Exception {
		ChangeScript changeScript = new ChangeScript(1, "script.sql", "doContent1", "undoContent1");

		applier.insertToSchemaVersionTable(changeScript);

		verify(schemaVersionManager).recordScriptApplied(changeScript);

	}

	@Test
	public void shouldCommitTransactionOnErrrCommitTransaction() throws Exception {
		applier.commitTransaction();

		verify(queryExecuter).commit();
	}


}
