package org.robotframework.filelibrary.keyword;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.robotframework.filelibrary.FileLibraryException;
import org.robotframework.filelibrary.context.TemplateContext;
import org.robotframework.filelibrary.util.CsvUtil;
import org.robotframework.filelibrary.util.TextUtil;
import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywordOverload;
import org.robotframework.javalib.annotation.RobotKeywords;

@RobotKeywords
public class ContextKeywords {

	@RobotKeyword("Clear the template data. All existing data. When the optional argument True is given; the cleared template data will be preloaded with test variables.")
	@ArgumentNames("initialize=")
	public void resetTemplateData(String variables) {
		TemplateContext.getInstance().reset();
		if (variables != null) {
			TemplateContext.getInstance().setValuesFromJSON(variables);
		}
		System.out.println("Template Context Reset.");
	}

	@RobotKeywordOverload
	public void resetTemplateData() {
		resetTemplateData(null);
	}

	@RobotKeyword("Get a specific value from the template data context.")
	@ArgumentNames("attribute")
	public String getTemplateData(String attributeName) {
		Object value = TemplateContext.getInstance().getValue(attributeName);
		String result = "";
		if (value != null) {
			result = value.toString();
		}
		System.out.println("Found value '" + result + "'");
		return result;
	}

	@RobotKeyword("Set a variable in the template context. The value can be either a string or a JSON string.")
	@ArgumentNames({ "attribute", "value" })
	public void setTemplateData(String name, String value) {

		if (TextUtil.isVariable(value)) {
			TemplateContext.getInstance().setValueFromTemplateData(name, value);
		} else {
			TemplateContext.getInstance().setValue(name, value);
		}
	}

	@RobotKeywordOverload
	public String logTemplateData() {
		return logTemplateData(null);
	}

	@RobotKeyword("Print all content of the template data as JSON. When an optional file name argument is supplied, the template context is also saved as a JSON File.")
	@ArgumentNames("outputFile=")
	public String logTemplateData(String outputFilePath) {

		String contextString = TemplateContext.getInstance().toJSON();
		System.out.println("Template context: \n" + contextString);

		if (outputFilePath != null) {
			try {
				File outputFile = new File(outputFilePath);
				FileWriter writer = new FileWriter(outputFile);
				writer.append(TemplateContext.getInstance().toJSON());
				writer.close();
				System.out.println("Created '" + outputFile.getAbsolutePath() + "'");
			} catch (IOException e) {
				throw new FileLibraryException(e);
			}
		}
		return contextString;
	}

	@RobotKeyword("Load template data from CSV.")
	@ArgumentNames({ "attribute", "csvFilePath" })
	public void setTemplateDataFromCSV(String variableName, String file) {

		List<?> records = CsvUtil.loadValuesFromFile(file);
		TemplateContext.getInstance().setValueList(variableName, records);
	}

}
