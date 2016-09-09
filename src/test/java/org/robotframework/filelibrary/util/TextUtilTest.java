package org.robotframework.filelibrary.util;

import org.junit.Assert;
import org.junit.Test;

public class TextUtilTest {

	@Test
	public void canDetectIndex() {

		Assert.assertTrue(TextUtil.containsIndex("test[1]"));
		Assert.assertTrue(TextUtil.containsIndex("test[1654765]"));
		Assert.assertFalse(TextUtil.containsIndex("test[165476a5]"));
		Assert.assertTrue(TextUtil.containsIndex("test[]"));
		Assert.assertFalse(TextUtil.containsIndex("[32]"));
	}

	@Test
	public void canExtractIndex() {

		Assert.assertEquals(0, TextUtil.getIndex("test"));
		Assert.assertEquals(1, TextUtil.getIndex("test[1]"));
		Assert.assertEquals(100, TextUtil.getIndex("test[100]"));
		Assert.assertEquals(-1, TextUtil.getIndex("test[]"));
		Assert.assertEquals(0, TextUtil.getIndex("test[1"));

	}

	@Test
	public void canRemoveIndex() {

		Assert.assertEquals("name", TextUtil.removeIndex("name[0]"));
		Assert.assertEquals("name", TextUtil.removeIndex("name"));
		Assert.assertEquals("name[1", TextUtil.removeIndex("name[1"));
	}

	@Test
	public void canAddSegment() {
		Assert.assertEquals("test", TextUtil.addSegment(null, "test"));
		Assert.assertEquals("a.b", TextUtil.addSegment("a", "b"));
		Assert.assertEquals("a", TextUtil.addSegment("a", null));
	}

	private void testPopulateIndex(String source, String target, String expected) {
		String[] result = TextUtil.populateIndexes(source, new String[] { target });
		Assert.assertEquals(expected, result[0]);
	}

	@Test
	public void canPopulateIndexes() {

		testPopulateIndex("a[1].b[2].c", "a[].d", "a[1].d");
		testPopulateIndex("a[1].b[2].c", "a[].b[].g", "a[1].b[2].g");
		testPopulateIndex("a[1].b[2].c", "a[0].b[0].g", "a[0].b[0].g");
	}
}
