package io.github.dvanherbergen.filelibrary.util;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.datatype.DataType;
import org.junit.Assert;
import org.junit.Test;

public class PrimaryKeyFilterTest {

	@Test
	public void canDetectPrimaryKeys() {
		
		PrimaryKeyFilter filter = new PrimaryKeyFilter(new String[] { "BESTELLR=COL1,COL4", "PROG=COL7"});
		
		Assert.assertTrue(filter.accept("BESTELLR", new Column("COL1", DataType.BIGINT)));
		Assert.assertFalse(filter.accept("BESTELLR", new Column("COL2", DataType.BIGINT)));
		Assert.assertFalse(filter.accept("BESTELLR2", new Column("COL1", DataType.BIGINT)));
		Assert.assertTrue(filter.accept("BESTELLR", new Column("COL4", DataType.BIGINT)));
		Assert.assertTrue(filter.accept("PROG", new Column("COL7", DataType.BIGINT)));
		Assert.assertFalse(filter.accept("PROG", new Column("COL3", DataType.BIGINT)));
	}
	
}
