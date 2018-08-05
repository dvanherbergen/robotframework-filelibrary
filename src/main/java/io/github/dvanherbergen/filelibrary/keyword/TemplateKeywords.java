package io.github.dvanherbergen.filelibrary.keyword;

import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;

import io.github.dvanherbergen.filelibrary.service.TemplateService;

@RobotKeywords
public class TemplateKeywords {

	private TemplateService service = new TemplateService();

	// @formatter:off
	@RobotKeyword("Generate a file based on a Freemarker template. Before using this keyword, you need to populate the Template Data using the 'Set Template Data...' keywords.\n" + 
			"All template data that is available can be referenced from within the Freemarker template using variables. E.g. ${soup.color}\n" + 
			"\n" + 
			"Usage:\n" + 
			"| Generate File | _templateFile_ | _outputFile_ |")
	@ArgumentNames({ "templateFile", "outputFile" })
	public void generateFile(String templateFile, String outputFile) {
		service.generateFile(templateFile, outputFile);
	}
}
