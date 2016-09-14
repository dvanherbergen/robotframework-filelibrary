package org.robotframework.filelibrary.util;

import java.util.HashSet;

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
		HashSet<String> filter = new HashSet<String>();
		filter.add("/messages/Message/Header");
		filter.add("/messages/Message/Payload/Payload/Object/Register/Id");
		CompareUtil.compareXMLFiles(getTestPath("source.xml"),
				getTestPath("template_ignored_node.xml"),filter);
	}

	@Test(expected = FileLibraryException.class)
	public void testMatchingTemplateWithIgnoredNodeStillContainingErrors() {
		CompareUtil.compareXMLFiles(getTestPath("source.xml"),
				getTestPath("template_ignored_nodes_errors.xml"));
	}
	
	@Test(expected = FileLibraryException.class)
	public void testNonMatchingTemplate() {
		CompareUtil.compareXMLFiles(getTestPath("source.xml"),
				getTestPath("invalid_template.xml"));
	}

	@Test(expected = FileLibraryException.class)
	public void testNonMatchingDueToMissingNodes() {
		CompareUtil.compareXMLFiles(getTestPath("source.xml"),
				getTestPath("missing_nodes.xml"));
	}
	
	private String getTestPath(String string) {
		return CompareUtilTest.class.getResource(string).getPath();
	}

}
