package com.dbdeploy.appliers;

import com.dbdeploy.ChangeScriptApplier;
import com.dbdeploy.database.DelimiterType;
import com.dbdeploy.exceptions.UsageException;
import com.dbdeploy.scripts.ChangeScript;
import com.dbdeploy.scripts.Script;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TemplateBasedApplier implements ChangeScriptApplier {
	private Configuration configuration;
	private Writer writer;
	private String syntax;
	private String changeLogTableName;
	private String delimiter;
	private DelimiterType delimiterType;
	private Script preScriptExecutionScript;
	private Script postScriptExecutionScript;


	public TemplateBasedApplier(Writer writer, String syntax, String changeLogTableName, String delimiter,
								DelimiterType delimiterType, File templateDirectory) throws IOException {
		this.syntax = syntax;
		this.changeLogTableName = changeLogTableName;
		this.delimiter = delimiter;
		this.delimiterType = delimiterType;
		this.writer = writer;
		this.configuration = new Configuration();

		FileTemplateLoader fileTemplateLoader = createFileTemplateLoader(templateDirectory);
		this.configuration.setTemplateLoader(
			new MultiTemplateLoader(
				new TemplateLoader[] {
					fileTemplateLoader,
					new ClassTemplateLoader(getClass(), "/"),
				}));
	}

	private FileTemplateLoader createFileTemplateLoader(File templateDirectory) throws IOException {
		if (templateDirectory == null) {
			return new FileTemplateLoader();
		} else {
			return new FileTemplateLoader(templateDirectory, true);
		}
	}

	public void apply(List<ChangeScript> changeScripts) {
		String filename = syntax + "_" + getTemplateQualifier() + ".ftl";

		try {
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("scripts", changeScripts);
			model.put("changeLogTableName", changeLogTableName);
			model.put("delimiter", delimiter);
			model.put("separator", (delimiterType == DelimiterType.row) ? "\n" : "");

			try {
				Template template = configuration.getTemplate(filename);
				template.process(model, writer);
			} finally {
				writer.close();
			}
		} catch (FileNotFoundException ex) {
			throw new UsageException("Could not find template named " + filename + "\n" +
				"Check that you have got the name of the database syntax correct.", ex);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String getTemplateQualifier() {
		return "apply";
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
