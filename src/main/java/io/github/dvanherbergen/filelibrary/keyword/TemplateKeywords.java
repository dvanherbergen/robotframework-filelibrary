package io.github.dvanherbergen.filelibrary.keyword;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;

import io.github.dvanherbergen.filelibrary.context.TemplateContext;
import io.github.dvanherbergen.filelibrary.service.DatabaseService;
import io.github.dvanherbergen.filelibrary.service.TemplateService;
import io.github.dvanherbergen.filelibrary.util.FileUtil;
import io.github.dvanherbergen.filelibrary.util.StatementParser;
import io.github.dvanherbergen.filelibrary.util.TextUtil;

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
	

	// @formatter:off
	@RobotKeyword("Load template data from SQL results. Specify either an SQL to execute or the path to a .sql file.\n" + 
			"\n" + 
			"Example uses:\n" + 
			"\n" + 
			"\n" + 
			"Usage:\n" + 
			"| Set Template Data From SQL | _attributePath_ | _sql_ |\n" + 
			"| Set Template Data From SQL | _attributePath_ | _sqlFile_ |\n" + 
			"Example usage:\n" + 
			"\n" + 
			"| Set Template Data From SQL | soup | soup-select.sql |\n" + 
			"| Set Template Data From SQL | soup | select brand, type, color from soups where id = 1 |\n" + 
			"| Set Template Data From SQL | soup.ingredients[] | select i.name from ingredients i, soup_ingredients si where i.id = si.ingredient_id and si.soup_id = 1 |\n" + 
			"| Set Template Data From SQL | soup.ingredients[].suppliers | select s.name from suppliers s, ingredient_suppliers is where s.id = is.supplier_id and is.ingredient_name = \\${soup.ingredients[].name}  |\n" + 
			"| Set Template Data From SQL | soup.ingredients[].suppliers[].contact | select c.phone from supplier_contact c where c.supplier_name = \\${soup.ingredients[].suppliers[].name} |\n" + 
			"\n\nThe examples shown above could result in the following template data structure:\n\n" + 
			"| { \"soup\": {\n" + 
			"|      \"brand\" : \"Campbells\",\n" + 
			"|      \"type\" : \"Tomato\",\n" + 
			"|      \"color\" : \"red\",\n" + 
			"|      \"ingredients\" : [\n" + 
			"|          {\n" + 
			"|             \"name\" : \"tomato\",\n" + 
			"|             \"suppliers\" : [\n" + 
			"|                { \n" + 
			"|                   \"name\" : \"the best tomato company\" ,\n" + 
			"|                   \"contact\" : {\n" + 
			"|                      \"phone\" : \"555-555.555\"\n" + 
			"|                   }\n" + 
			"|                },\n" + 
			"|                { \n" + 
			"|                   \"name\" : \"the second best tomato company\",\n" + 
			"|                   \"contact\" : {\n" + 
			"|                      \"phone\" : \"666-666.666\"\n" + 
			"|                   }\n" + 
			"|                }\n" + 
			"|             ]\n" + 
			"|          },\n" + 
			"|          {\n" + 
			"|             \"name\" : \"potato\",\n" + 
			"|             \"suppliers\" : [\n" + 
			"|                { \"name\" : \"the only potato company\" }\n" + 
			"|             ]            \n" + 
			"|          },\n" + 
			"|          {\n" + 
			"|             \"name\" : \"water\",\n" + 
			"|             \"suppliers\" : []            \n" + 
			"|          },\n" + 
			"|          {\n" + 
			"|             \"name\" : \"pepper\",\n" + 
			"|             \"suppliers\" : [\n" + 
			"|                { \"name\" : \"the sweet pepper company\" },\n" + 
			"|                { \"name\" : \"the spicy pepper company\" }\n" + 
			"|             ]               \n" + 
			"|          }\n" + 
			"|       ]\n" + 
			"|    }\n" + 
			"| }"  
		)
	// @formatter:on
	@ArgumentNames({ "attribute", "sql" })
	public void setTemplateDataFromSQL(String targetAttributePath, String sql) {

		boolean targetIsList = TemplateContext.isListTarget(targetAttributePath);

		List<String> sqls = new ArrayList<>();
		if (FileUtil.isSqlFileName(sql)) {
			sqls = FileUtil.parseSQLStatements(sql);
		} else {
			sqls.add(sql);
		}

		for (String attributePath : TemplateContext.getInstance().expandTargetAttributes(targetAttributePath)) {

			for (String stmt : sqls) {
				StatementParser parser = new StatementParser(stmt);
				String[] parameters = TextUtil.populateIndexes(attributePath, parser.getParameters());
				parameters = TemplateContext.getInstance().resolveAttributes(parameters);
				List<Map<String, Object>> records = DatabaseService.getInstance().getQueryResultsAsMap(parser.getStatement(), parameters,
						(targetIsList ? 0 : 1));
				System.out.println("Query returned " + records.size() + " result.");
				if (!records.isEmpty()) {
					if (targetIsList) {
						// populate an data attribute with a list of records
						TemplateContext.getInstance().setValueList(attributePath, records);
					} else {
						// populate an data attribute with the values of a
						// single record
						TemplateContext.getInstance().setValue(attributePath, records.get(0));
					}
				}
			}
		}
	}
}
