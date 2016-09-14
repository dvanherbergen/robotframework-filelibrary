package org.robotframework.filelibrary.keyword;

import java.util.Arrays;
import java.util.HashSet;

import org.robotframework.filelibrary.util.CompareUtil;
import org.robotframework.filelibrary.util.FileUtil;
import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;

@RobotKeywords
public class FileKeywords {

	// @formatter:off
	@RobotKeyword("Concatenate multiple files into a single file.\n"
			+ "\n"
			+ "Usage:\n"
			+ "| Concatenate Files | _targetFile_ | _sourceFile1_ | _sourceFile2_ |\n"
			+ "| Concatenate Files | _targetFile_ | _sourceFile1_ | _sourceFile2_ | _sourceFile3_ |")
	@ArgumentNames({ "targetFile", "*sourceFiles" })
	public void concatenateFiles(String targetFile, String... sourceFiles) {
		FileUtil.appendFiles(targetFile, sourceFiles);
	}

	@RobotKeyword("Verify that the content of two files matches. Files are compared line by line.\n"
			+ "\n"
			+ "Usage:\n"
			+ "| Verify Files Are Equal | _file1_ | _file2_ |")
	@ArgumentNames({ "file1", "file2" })
	public void verifyFilesAreEqual(String file1, String file2) {
		CompareUtil.compareFiles(file1, file2);
	}


	@RobotKeyword("Verify that the content of two xml files matches.\n Whitespace and formatting is ignored.\n"
			+ "Replace values of nodes or attributes with the string #{IGNORE} to exclude them from comparisson.\n"
			+ "Provide xpath filters to exclude certain nodes from comparison. (only basic xpath support)\n"
			+ "\n"
			+ "Usage:\n"
			+ "| Verify XML Files Are Equal | _file1_ | _file2_ |\n"
			+ "| Verify XML Files Are Equal | _file1_ | _file2_ | _xpathFilter1_ \n"
			+ "| Verify XML Files Are Equal | _file1_ | _file2_ | _xpathFilter1_ | _xpathFilter2_ ")
	@ArgumentNames({ "file1", "file2", "*filters" })
	public void verifyXMLFilesAreEqual(String file1, String file2, String... filters) {
		HashSet<String> filterSet = new HashSet<String>();
		filterSet.addAll(Arrays.asList(filters));
		CompareUtil.compareXMLFiles(file1, file2, filterSet);
	}
	
	
	@RobotKeyword("Compress multiple files into a single zip file.\n"
			+ "\n"
			+ "Usage:\n"
			+ "| Compress Files | _targetFile_ | _sourceFile1_ |  | |\n"
			+ "| Compress Files | _targetFile_ | _sourceFile1_ | _sourceFile2_ | _sourceFile3_ |")
	@ArgumentNames({ "targetFile", "*sourceFiles" })
	public void zipFiles(String targetFile, String... sourceFiles) {
		FileUtil.compressFiles(targetFile, sourceFiles);
	}
}
