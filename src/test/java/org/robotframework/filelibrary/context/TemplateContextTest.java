package org.robotframework.filelibrary.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

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
		String json = "{\n" + "  \"name\" : \"Mr. Robot\",\n" + "  \"address\" : {\n"
				+ "    \"street\" : \"High Street\",\n" + "    \"number\" : \"13\",\n" + "    \"postal\" : \"UX8\"\n"
				+ "  }\n" + "}";
		TemplateContext context = new TemplateContext();
		context.setValue("user", json);

		Map<String, Object> user = (Map<String, Object>) context.getValues().get("user");
		Assert.assertEquals("Mr. Robot", user.get("name"));
		Map<String, Object> address = (Map<String, Object>) user.get("address");
		Assert.assertEquals("High Street", address.get("street"));
		Assert.assertEquals("13", address.get("number"));

	}

	@Test
	public void canInitializeVariablesFromJSON() {
		String json = "{\n" + "  \"name\" : \"Mr. Robot\",\n" + "  \"address\" : {\n"
				+ "    \"street\" : \"High Street\",\n" + "    \"number\" : \"13\",\n" + "    \"postal\" : \"UX8\"\n"
				+ "  }\n" + "}";
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
		context.setValues("users", list);

		Map<String, String> user1 = (Map<String, String>) ((List) context.getValues().get("users")).get(0);
		Map<String, String> user2 = (Map<String, String>) ((List) context.getValues().get("users")).get(1);
		Assert.assertEquals("1", user1.get("id"));
		Assert.assertEquals("2", user2.get("id"));
	}

	@Test
	public void canGetValue() {

		String json = "{\n" + "  \"criteria\" : {\n" + "    \"access_point\" : \"541454823041091814\",\n"
				+ "    \"al_account_id\" : \"43940250\",\n" + "    \"al_acct_location_id\" : \"360467025\"\n" + "  },\n"
				+ "  \"now\" : 1472724173166,\n" + "  \"name\" : \"My First Test\"\n" + "}";
		TemplateContext context = new TemplateContext();
		context.setValuesFromJSON(json);

		Assert.assertEquals("541454823041091814", context.getValue("criteria.access_point"));
		Assert.assertEquals("43940250", context.getValue("criteria.al_account_id"));
		Assert.assertEquals("My First Test", context.getValue("name"));
		Assert.assertEquals("", context.getValue("criteria.ghost"));
		Assert.assertEquals("", context.getValue("criteria.access_point.ghost"));
		Assert.assertEquals("", context.getValue("ghost.value"));

	}
}
