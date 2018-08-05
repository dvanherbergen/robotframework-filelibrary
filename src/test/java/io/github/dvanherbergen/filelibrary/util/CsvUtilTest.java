package io.github.dvanherbergen.filelibrary.util;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import io.github.dvanherbergen.filelibrary.FileLibraryException;
import junit.framework.Assert;

public class CsvUtilTest {

	@Test
	public void canReadCsvFile() {

		List<Map<String, Object>> results = CsvUtil.loadValuesFromFile("src/test/resources/input.csv");

		Assert.assertEquals(3, results.size());

		Map<String, Object> row1 = results.get(0);
		Map<String, Object> row2 = results.get(1);
		Map<String, Object> row3 = results.get(2);
		Assert.assertEquals("Jef", row1.get("name"));
		Assert.assertEquals("Jacques", row2.get("name"));
		Assert.assertEquals("Jeff", row3.get("name"));
		Assert.assertEquals("1", row1.get("id"));
		Assert.assertEquals("2", row2.get("id"));
		Assert.assertEquals("3", row3.get("id"));

	}

	@Test
	public void throwsErrorOnInvalidFile() {

		try {
			CsvUtil.loadValuesFromFile("does_not_exist");
			Assert.fail("Missing exception.");
		} catch (FileLibraryException e) {
			Assert.assertTrue(e.getMessage().startsWith("Cannot find file"));
		}
	}

}
