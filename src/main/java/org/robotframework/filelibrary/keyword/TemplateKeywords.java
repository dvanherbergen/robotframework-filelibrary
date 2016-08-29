package org.robotframework.filelibrary.keyword;

import org.robotframework.filelibrary.template.TemplateService;
import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;

@RobotKeywords
public class TemplateKeywords {

	private TemplateService service = new TemplateService();

	@RobotKeyword("Generate a file from template.")
	@ArgumentNames({ "templateFile", "outputFile" })
	public void generateFile(String templateFile, String outputFile) {
		service.generateFile(templateFile, outputFile);
	}
}
