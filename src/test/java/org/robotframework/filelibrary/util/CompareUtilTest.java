package org.robotframework.filelibrary.util;

import org.junit.Ignore;
import org.junit.Test;
import org.robotframework.filelibrary.FileLibraryException;

public class CompareUtilTest {

	@Test
	public void testCompareOfIdenticalXMLFiles() {
		CompareUtil.compareXMLFiles(getTestPath("source.xml"),
				getTestPath("source.xml"));
	}

	@Test
	@Ignore("Not yet supported")
	public void testWhitespaceIsIgnored() {
		CompareUtil.compareXMLFiles(getTestPath("source.xml"),
				getTestPath("different_white_space.xml"));
	}

	@Test(expected = FileLibraryException.class)
	public void testDifferentFiles() {
		CompareUtil.compareXMLFiles(getTestPath("source.xml"),
				getTestPath("different_file.xml"));
	}

	@Test
	public void testMatchingTemplate() {
		CompareUtil.compareXMLFiles(getTestPath("source.xml"),
				getTestPath("template.xml"));
	}

	@Test
	public void testMatchingTemplateWithIgnoredNode() {
		CompareUtil.compareXMLFiles(getTestPath("source.xml"),
				getTestPath("template_ignored_node.xml"));
	}
	
	@Test(expected = FileLibraryException.class)
	public void testNonMatchingTemplate() {
		CompareUtil.compareXMLFiles(getTestPath("source.xml"),
				getTestPath("invalid_template.xml"));
	}
	
	private String getTestPath(String string) {
		return CompareUtilTest.class.getResource(string).getPath();
	}

}
