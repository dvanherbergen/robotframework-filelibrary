package org.robotframework.filelibrary.util;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.robotframework.filelibrary.FileLibraryException;

public class FileUtilTest {

	@Test
	public void canReadSingleStatement() {
		List<String> statements = FileUtil.parseSQLStatements("src/test/resources/FileUtil/single.sql");
		Assert.assertEquals(1, statements.size());
		Assert.assertEquals("select A from B", statements.get(0));
	}

	@Test
	public void canReadMultipleStatements() {
		List<String> statements = FileUtil.parseSQLStatements("src/test/resources/FileUtil/multiple.sql");
		Assert.assertEquals(4, statements.size());
		Assert.assertEquals("select A from B", statements.get(0));
		Assert.assertEquals("select B from C", statements.get(1));
		Assert.assertEquals("select C from D", statements.get(2));
		Assert.assertEquals("select D from E", statements.get(3));
	}

	@Test(expected = FileLibraryException.class)
	public void failsOnMissingStatement() {
		FileUtil.parseSQLStatements("src/test/resources/FileUtil/empty.sql");
	}

	@Test(expected = FileLibraryException.class)
	public void failsOnMissingFile() {
		FileUtil.parseSQLStatements("src/test/resources/FileUtil/dummy.sql");
	}

	@Test
	public void canDetectSqlFilenam() {
		Assert.assertEquals(true, FileUtil.isSqlFileName("c:\\test\\resource\\myfile.sql"));
		Assert.assertEquals(true, FileUtil.isSqlFileName("c:\\test\\resource\\myfile.SQL  "));
		Assert.assertEquals(false, FileUtil.isSqlFileName("c:\\test\\resource\\myfile.csv"));
		Assert.assertEquals(false, FileUtil.isSqlFileName(""));
		Assert.assertEquals(false, FileUtil.isSqlFileName(null));
		Assert.assertEquals(false, FileUtil.isSqlFileName(""));
		Assert.assertEquals(false, FileUtil.isSqlFileName("select * from table"));

	}
}
