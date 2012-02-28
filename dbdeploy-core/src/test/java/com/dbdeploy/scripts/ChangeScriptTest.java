package com.dbdeploy.scripts;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;

public class ChangeScriptTest {
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenNoIdLessThanZeroGiven() {
		new ChangeScript(-1, "description", "doContent", "undoContent");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWenNoDescriptionGiven() {
		new ChangeScript(1, null, "doContent", "undoContent");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWenNoDoContentGiven() {
		new ChangeScript(1, "description", null, "undoContent");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWenNoUndoContentGiven() {
		new ChangeScript(1, "description", "doContent", null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenNoFileGiven() {
		new ChangeScript(1, null, "UTF-8");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionWhenNoEncodingGiven() throws IOException {
		new ChangeScript(1, createTemporaryFileWithContent("test"), null);
	}
	
	@Test
	public void changeScriptsHaveAnId() throws Exception {
		final File file = createTemporaryFileWithContent("Hello\nThere!\n");
		
		final ChangeScript changeScript = new ChangeScript(5, file, "UTF-8");
		
		assertThat(changeScript.getId(), equalTo(5L));
	}

	@Test
	public void shouldReturnContentsOfFile() throws Exception {
		final File file = createTemporaryFileWithContent("Hello\nThere!\n");

		ChangeScript changeScript = new ChangeScript(5, file, "UTF-8");
		
		assertThat(changeScript.getContent(), is("Hello\nThere!\n"));
	}

	@Test
	public void shouldReturnChecksumOfFile() throws Exception {
		final File file = createTemporaryFileWithContent("Hello world.\n");
		
		ChangeScript changeScript = new ChangeScript(5, file, "UTF-8");
		
		assertThat(changeScript.getChecksum(), is(notNullValue()));
	}
	
	@Test
	public void shouldNotReturnTheChecksumOfHelloThereMessageWhenHelloWorldIsGiven() throws Exception {
		final File file = createTemporaryFileWithContent("Hello world.\n");
		
		ChangeScript changeScript = new ChangeScript(5, file, "UTF-8");
		
		assertThat(changeScript.getChecksum(), is(not("88749cf876ecae2eaea44e484e2e03d3d81debb7d91e1d0f6aff6e9b7b07e56e")));
	}
	
	@Test
	public void shouldReturnValidChecksumOfFile() throws Exception {
		final File file = createTemporaryFileWithContent("Hello\nThere!\n");
		
		ChangeScript changeScript = new ChangeScript(5, file, "UTF-8");

		assertThat(changeScript.getChecksum(), is("88749cf876ecae2eaea44e484e2e03d3d81debb7d91e1d0f6aff6e9b7b07e56e"));
	}
	
	@Test
	public void contentsOfFileShouldExcludeAnythingAfterAnUndoMarker() throws Exception {
		final File file = createTemporaryFileWithContent(
				"Hello\n" +
				"There!\n" +
				"--//@UNDO\n" +
				"This is after the undo marker!\n");

		final ChangeScript changeScript = new ChangeScript(5, file, "UTF-8");
		
		assertThat(changeScript.getContent(), is("Hello\nThere!\n"));
	}

	@Test
	public void contentsOfFileShouldExcludeAnythingAfterAnUndoMarkerEvenWhenThatMarkerHasSomeWhitespaceAtTheEnd() throws Exception {
		final File file = createTemporaryFileWithContent(
				"Hello\n" +
				"There!\n" +
				"--//@UNDO   \n" +
				"This is after the undo marker!\n");

		final ChangeScript changeScript = new ChangeScript(5, file, "UTF-8");
		
		assertThat(changeScript.getContent(), is("Hello\nThere!\n"));
	}

	@Test
	public void shouldReturnUndoContentsOfFile() throws Exception {
		final File file = createTemporaryFileWithContent(
				"Hello\n" +
				"There!\n" +
				"--//@UNDO\n" +
				"This is after the undo marker!\n");

		final ChangeScript changeScript = new ChangeScript(5, file, "UTF-8");
		
		assertThat(changeScript.getUndoContent(), is("This is after the undo marker!\n"));		
	}

	@Test
	public void changeScriptsNaturallyOrderById() throws Exception {
		final ChangeScript one = new ChangeScript(1, "description1", "doContent1", "undoContent1");
		final ChangeScript two = new ChangeScript(2, "description2", "doContent2", "undoContent2");

		assertThat(one.compareTo(two), lessThan(1));
		assertThat(two.compareTo(one), greaterThanOrEqualTo(1));
	}

	@Test
	public void toStringReturnsASensibleValue() throws Exception {
		final ChangeScript changeScript = new ChangeScript(5, "description1.txt", "doContent1", "undoContent1");
		
		assertThat(changeScript.toString(), equalTo("#5: description1.txt"));
	}

	@Test
	public void shouldUseFileNameAsDescription() throws Exception {
		final File file = createTemporaryFileWithContent("SELECT 1 FROM dual;");
		
		final ChangeScript changeScript = new ChangeScript(5, file, "UTF-8");
		
		assertThat(changeScript.toString(), equalTo("#5: " + file.getName()));
	}
	
	private File createTemporaryFileWithContent(final String content) throws IOException {
		final File file = File.createTempFile("changeScriptTest", ".sql");
		file.deleteOnExit();

		final BufferedWriter out = new BufferedWriter(new FileWriter(file));
		out.write(content);
		out.close();
		
		return file;
	}
}
