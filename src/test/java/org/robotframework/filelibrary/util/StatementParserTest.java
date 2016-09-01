package org.robotframework.filelibrary.util;

import java.util.List;

import org.junit.Test;

import junit.framework.Assert;

public class StatementParserTest {

	@Test
	public void canExtractParameters() {
		StatementParser util = new StatementParser("${color} is the ${level} color of ${scope}");
		List<String> results = util.getParameters();
		Assert.assertEquals("color", results.get(0));
		Assert.assertEquals("level", results.get(1));
		Assert.assertEquals("scope", results.get(2));
		Assert.assertEquals(3, results.size());
	}

	@Test
	public void canReplaceParameters() {
		StatementParser util = new StatementParser("${color} is the ${level} color of ${scope}");
		Assert.assertEquals("? is the ? color of ?", util.getStatement());
	}

	@Test
	public void canTrimSemicolon() {
		StatementParser util = new StatementParser("${color} is the ${level} color of ${scope} ; ");
		Assert.assertEquals("? is the ? color of ?", util.getStatement());
	}
}
