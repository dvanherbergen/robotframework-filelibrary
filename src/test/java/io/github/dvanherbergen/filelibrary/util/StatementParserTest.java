package io.github.dvanherbergen.filelibrary.util;

import org.junit.Test;

import junit.framework.Assert;

public class StatementParserTest {

	@Test
	public void canExtractParameters() {
		StatementParser util = new StatementParser("${color} is the ${level} color of ${scope}");
		String[] results = util.getParameters();
		Assert.assertEquals("color", results[0]);
		Assert.assertEquals("level", results[1]);
		Assert.assertEquals("scope", results[2]);
		Assert.assertEquals(3, results.length);
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
