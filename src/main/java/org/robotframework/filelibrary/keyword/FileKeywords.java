package org.robotframework.filelibrary.keyword;

import org.robotframework.filelibrary.util.FileUtil;
import org.robotframework.javalib.annotation.ArgumentNames;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;

@RobotKeywords
public class FileKeywords {

	@RobotKeyword("Concatenate multiple files into a single file.")
	@ArgumentNames({ "targetFile", "*sourceFiles" })
	public void concatenateFiles(String targetFile, String... sourceFiles) {
		FileUtil.appendFiles(targetFile, sourceFiles);
	}

	@RobotKeyword("Compare the content of two files.")
	@ArgumentNames({ "file1", "file2" })
	public void verifyFilesAreEqual(String file1, String file2) {
		FileUtil.compareFiles(file1, file2);
	}

	@RobotKeyword("Compress multiple files into a single zip file.")
	@ArgumentNames({ "targetFile", "*sourceFiles" })
	public void zipFiles(String targetFile, String... sourceFiles) {
		FileUtil.compressFiles(targetFile, sourceFiles);
	}
}
