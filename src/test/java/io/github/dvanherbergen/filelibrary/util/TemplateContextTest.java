package io.github.dvanherbergen.filelibrary.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.dvanherbergen.filelibrary.context.TemplateContext;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TemplateContextTest {

	@Test
	public void variableCanBeSet() {
		TemplateContext context = new TemplateContext();
		context.setValue("user", "name");
		Assert.assertEquals("name", context.getValues().get("user"));
	}

	@Test
	public void nestedVariableCanBeSet() {
		TemplateContext context = new TemplateContext();
		context.setValue("cars.ford.mustang", "2016");
		Map<String, Object> level1 = (Map<String, Object>) context.getValues().get("cars");
		Map<String, Object> level2 = (Map<String, Object>) level1.get("ford");
		Assert.assertEquals("2016", level2.get("mustang"));
	}

	@Test
	public void valuesCanBeCloned() throws Exception {
		TemplateContext context = TemplateContext.getInstance();
		context.setValue("B", "{\"key\" : \"102\", \"value\" : \"B\"}");
		context.setValue("C", "{\"key\" : \"103\", \"value\" : \"C\"}");
		assertValue(context, "B.key", "102");
		assertValue(context, "B.value", "B");
		assertValue(context, "C.key", "103");
		assertValue(context, "C.value", "C");

		log(context);

		context.setValueFromTemplateData("A", "${B}");
		log(context);

		assertValue(context, "A.key", "102");
		assertValue(context, "A.value", "B");

		assertValue(context, "B.key", "102");
		assertValue(context, "B.value", "B");
		assertValue(context, "C.key", "103");
		assertValue(context, "C.value", "C");

		context.setValueFromTemplateData("A", "${C}");
		log(context);

		assertValue(context, "A.key", "103");
		assertValue(context, "A.value", "C");

		assertValue(context, "B.key", "102");
		assertValue(context, "B.value", "B");
		assertValue(context, "C.key", "103");
		assertValue(context, "C.value", "C");

	}

	private void log(TemplateContext context) throws Exception {
		System.out.println("---- Current context = " + new ObjectMapper().writeValueAsString(context.getValues()));
	}

	private void assertValue(TemplateContext context, String name, String value) {
		String obj = (String) context.getValue(name);
		Assert.assertEquals(value, obj);
	}

	@Test
	public void canSetArrayValues() throws Exception {
		TemplateContext context = new TemplateContext();
		context.setValue("Records[0].test", "test0");
		log(context);
		context.setValue("Records[1].test", "test1");
		log(context);
		context.setValue("Records[2].test", "test2");

		log(context);
		assertValue(context, "Records[0].test", "test0");
		assertValue(context, "Records[1].test", "test1");
		assertValue(context, "Records[2].test", "test2");
	}

	@Test
	public void nestedVariableCanBeAdded() {
		TemplateContext context = new TemplateContext();
		context.setValue("cars.ford.mustang", "2016");
		context.setValue("cars.ford.focus", "2017");
		Map<String, Object> level1 = (Map<String, Object>) context.getValues().get("cars");
		Map<String, Object> level2 = (Map<String, Object>) level1.get("ford");
		Assert.assertEquals("2017", level2.get("focus"));
		Assert.assertEquals("2016", level2.get("mustang"));
	}

	@Test
	public void nestedVariableCanBeReplaced() {
		TemplateContext context = new TemplateContext();
		context.setValue("cars.ford.mustang", "2016");
		context.setValue("cars.ford.mustang", "2017");
		Map<String, Object> level1 = (Map<String, Object>) context.getValues().get("cars");
		Map<String, Object> level2 = (Map<String, Object>) level1.get("ford");
		Assert.assertEquals("2017", level2.get("mustang"));
	}

	@Test
	public void canSetVariableWithJSONData() {
		String json = "{\n" + "  \"name\" : \"Mr. Robot\",\n" + "  \"address\" : {\n" + "    \"street\" : \"High Street\",\n"
				+ "    \"number\" : \"13\",\n" + "    \"postal\" : \"UX8\"\n" + "  }\n" + "}";
		TemplateContext context = new TemplateContext();
		context.setValue("user", json);

		Map<String, Object> user = (Map<String, Object>) context.getValues().get("user");
		Assert.assertEquals("Mr. Robot", user.get("name"));
		Map<String, Object> address = (Map<String, Object>) user.get("address");
		Assert.assertEquals("High Street", address.get("street"));
		Assert.assertEquals("13", address.get("number"));

	}

	@Test
	public void canSetValueInList() {
		// @formatter:off
		String json = "{ \"data\" : [\n" + 
				"{ \"name\" : \"a\", \"elements\" : [ { \"name\" : \"a1\" }, { \"name\" : \"a2\" } ] },\n" + 
				"{ \"name\" : \"b\", \"elements\" : [ { \"name\" : \"b1\" }, { \"name\" : \"b2\" } ] },\n" + 
				"{ \"name\" : \"c\", \"elements\" : [] },\n" + 
				"{ \"name\" : \"d\" }]}";
		// @formatter:on
		TemplateContext context = new TemplateContext();
		context.setValuesFromJSON(json);

		context.setValue("data[1].value", "test1");
		Assert.assertEquals("test1", context.getValue("data[1].value"));

		context.setValue("data[1].elements[1].value", "test1");
		Assert.assertEquals("test1", context.getValue("data[1].elements[1].value"));

	}

	@Test
	public void canInitializeVariablesFromJSON() {
		String json = "{\n" + "  \"name\" : \"Mr. Robot\",\n" + "  \"address\" : {\n" + "    \"street\" : \"High Street\",\n"
				+ "    \"number\" : \"13\",\n" + "    \"postal\" : \"UX8\"\n" + "  }\n" + "}";
		TemplateContext context = new TemplateContext();
		context.setValuesFromJSON(json);

		Assert.assertEquals("Mr. Robot", context.getValues().get("name"));
		Map<String, Object> address = (Map<String, Object>) context.getValues().get("address");
		Assert.assertEquals("High Street", address.get("street"));
		Assert.assertEquals("13", address.get("number"));

	}

	@Test
	public void canInitializeVariableWithList() {

		List<Map<String, String>> list = new ArrayList<>();
		Map<String, String> map1 = new HashMap<>();
		Map<String, String> map2 = new HashMap<>();
		list.add(map1);
		list.add(map2);
		map1.put("name", "Leo");
		map1.put("id", "1");
		map2.put("name", "Leona");
		map2.put("id", "2");

		TemplateContext context = new TemplateContext();
		context.setValueList("users", list);

		Map<String, String> user1 = (Map<String, String>) ((List) context.getValues().get("users")).get(0);
		Map<String, String> user2 = (Map<String, String>) ((List) context.getValues().get("users")).get(1);
		Assert.assertEquals("1", user1.get("id"));
		Assert.assertEquals("2", user2.get("id"));
	}

	@Test
	public void canGetValue() {

		String json = "{\n" + "  \"criteria\" : {\n" + "    \"access_point\" : \"541454823041091814\",\n" + "    \"al_account_id\" : \"43940250\",\n"
				+ "    \"al_acct_location_id\" : \"360467025\"\n" + "  },\n" + "  \"now\" : 1472724173166,\n" + "  \"name\" : \"My First Test\"\n"
				+ "}";
		TemplateContext context = new TemplateContext();
		context.setValuesFromJSON(json);

		Assert.assertEquals("541454823041091814", context.getValue("criteria.access_point"));
		Assert.assertEquals("43940250", context.getValue("criteria.al_account_id"));
		Assert.assertEquals("My First Test", context.getValue("name"));
		Assert.assertEquals("", context.getValue("criteria.ghost"));
		Assert.assertEquals("", context.getValue("criteria.access_point.ghost"));
		Assert.assertEquals("", context.getValue("ghost.value"));

	}

	@Test
	public void canGetValueByIndex() {

		// @formatter:off
		String json = "{ \"data\" : [\n" + 
				"{ \"name\" : \"a\", \"elements\" : [ { \"name\" : \"a1\" }, { \"name\" : \"a2\" } ] },\n" + 
				"{ \"name\" : \"b\", \"elements\" : [ { \"name\" : \"b1\" }, { \"name\" : \"b2\" } ] },\n" + 
				"{ \"name\" : \"c\", \"elements\" : [] },\n" + 
				"{ \"name\" : \"d\" }]}";
		// @formatter:on
		TemplateContext context = new TemplateContext();
		context.setValuesFromJSON(json);

		Assert.assertEquals("a", context.getValue("data[0].name"));
		Assert.assertEquals("a1", context.getValue("data[0].elements[0].name"));
		Assert.assertEquals("a2", context.getValue("data[0].elements[1].name"));
		Assert.assertEquals("b", context.getValue("data[1].name"));
		Assert.assertEquals("b1", context.getValue("data[1].elements[0].name"));
		Assert.assertEquals("b2", context.getValue("data[1].elements[1].name"));
		Assert.assertEquals("c", context.getValue("data[2].name"));
		Assert.assertEquals("", context.getValue("data[2].elements[0].name"));
		Assert.assertEquals("d", context.getValue("data[3].name"));
		Assert.assertEquals("", context.getValue("data[3].elements[0].name"));

	}

	@Test
	public void canExpandAttributePaths() {
		// @formatter:off
		String json = "{ \"soup\": {\n" + 
				"     \"brand\" : \"Campbells\",\n" + 
				"     \"type\" : \"Tomato\",\n" + 
				"     \"color\" : \"red\",\n" + 
				"     \"ingredients\" : [\n" + 
				"         {\n" + 
				"            \"name\" : \"tomato\",\n" + 
				"            \"suppliers\" : [\n" + 
				"               {\n" + 
				"                  \"name\" : \"the best tomato company\",\n" + 
				"                  \"contact\" : {\n" + 
				"                     \"phone\" : \"555-555.555\"\n" + 
				"                  }\n" + 
				"               },\n" + 
				"               {\n" + 
				"                  \"name\" : \"the second best tomato company\",\n" + 
				"                  \"contact\" : {\n" + 
				"                     \"phone\" : \"666-666.666\"\n" + 
				"                  }\n" + 
				"               }\n" + 
				"            ]\n" + 
				"         },\n" + 
				"         {\n" + 
				"            \"name\" : \"potato\",\n" + 
				"            \"suppliers\" : [\n" + 
				"               { \"name\" : \"the only potato company\" }\n" + 
				"            ]\n" + 
				"         },\n" + 
				"         {\n" + 
				"            \"name\" : \"water\",\n" + 
				"            \"suppliers\" : []\n" + 
				"         },\n" + 
				"         {\n" + 
				"            \"name\" : \"pepper\",\n" + 
				"            \"suppliers\" : [\n" + 
				"               { \"name\" : \"the sweet pepper company\" },\n" + 
				"               { \"name\" : \"the spicy pepper company\" }\n" + 
				"            ]\n" + 
				"         }\n" + 
				"      ]\n" + 
				"   }\n" + 
				"}";
		
		// @formatter:on
		TemplateContext context = new TemplateContext();
		context.setValuesFromJSON(json);

		List<String> expandedValues = context.expandTargetAttributes("soup.ingredients[].suppliers");
		Assert.assertEquals(4, expandedValues.size());
		Assert.assertEquals("soup.ingredients[0].suppliers", expandedValues.get(0));
		Assert.assertEquals("soup.ingredients[1].suppliers", expandedValues.get(1));
		Assert.assertEquals("soup.ingredients[2].suppliers", expandedValues.get(2));
		Assert.assertEquals("soup.ingredients[3].suppliers", expandedValues.get(3));

		expandedValues = context.expandTargetAttributes("soup.ingredients[].suppliers[].contact");
		Assert.assertEquals(5, expandedValues.size());
		Assert.assertEquals("soup.ingredients[0].suppliers[0].contact", expandedValues.get(0));
		Assert.assertEquals("soup.ingredients[0].suppliers[1].contact", expandedValues.get(1));
		Assert.assertEquals("soup.ingredients[1].suppliers[0].contact", expandedValues.get(2));
		Assert.assertEquals("soup.ingredients[3].suppliers[0].contact", expandedValues.get(3));
		Assert.assertEquals("soup.ingredients[3].suppliers[1].contact", expandedValues.get(4));

		expandedValues = context.expandTargetAttributes("soup.ingredients[1].suppliers[].contact");
		Assert.assertEquals(1, expandedValues.size());
		Assert.assertEquals("soup.ingredients[1].suppliers[0].contact", expandedValues.get(0));

		expandedValues = context.expandTargetAttributes("soup.ingredients[0].suppliers[0].contact");
		Assert.assertEquals(1, expandedValues.size());
		Assert.assertEquals("soup.ingredients[0].suppliers[0].contact", expandedValues.get(0));

		expandedValues = context.expandTargetAttributes("soup.ingredients[].suppliers[]");
		Assert.assertEquals(4, expandedValues.size());
		Assert.assertEquals("soup.ingredients[0].suppliers[]", expandedValues.get(0));
		Assert.assertEquals("soup.ingredients[1].suppliers[]", expandedValues.get(1));
		Assert.assertEquals("soup.ingredients[2].suppliers[]", expandedValues.get(2));
		Assert.assertEquals("soup.ingredients[3].suppliers[]", expandedValues.get(3));

		expandedValues = context.expandTargetAttributes("soup.brand[].suppliers[]");
		Assert.assertEquals(0, expandedValues.size());

		expandedValues = context.expandTargetAttributes("data.test[]");
		Assert.assertEquals(1, expandedValues.size());
		Assert.assertEquals("data.test[]", expandedValues.get(0));

		expandedValues = context.expandTargetAttributes("data.data2.data3.test[]");
		Assert.assertEquals(1, expandedValues.size());
		Assert.assertEquals("data.data2.data3.test[]", expandedValues.get(0));

		expandedValues = context.expandTargetAttributes("data.data2.data3");
		Assert.assertEquals(1, expandedValues.size());
		Assert.assertEquals("data.data2.data3", expandedValues.get(0));
	}
}
