package io.github.dvanherbergen.filelibrary.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.filter.IColumnFilter;

import io.github.dvanherbergen.filelibrary.FileLibraryException;

/**
 * Custom primary key filter for DBUNIT. Used when the tables do not have primary keys defined.
 */
public class PrimaryKeyFilter implements IColumnFilter {

	private Map<String, List<String>> tableKeys = new HashMap<>();
	
	/**
	 * Create a new primary key filter.
	 * Each primary key should be defined in the format
	 * TABLE_NAME=KEY,OTHER_KEY,...
	 * 
	 * @param primaryKeys
	 */
	public PrimaryKeyFilter(String[] primaryKeys) {
		
		for (String definition : primaryKeys) {
			String[] tmp = definition.split("=");
			if (tmp.length != 2) {
				throw new FileLibraryException("Primary '"  + definition + "' not in the correct format: TABLE=KEY[,KEY]");
			}
			String table = tmp[0].toUpperCase();
			List<String> keys = new ArrayList<>();
			String[] keyDefintions = tmp[1].split(",");
			for (String key : keyDefintions) {
				keys.add(key.toUpperCase());
			}
			tableKeys.put(table, keys);
		}

	}

	@Override
	public boolean accept(String tableName, Column column) {
		
		List<String> keys = tableKeys.get(tableName.toUpperCase());
		if (keys == null) {
			return false;
		}
		
		boolean isKey = keys.contains(column.getColumnName().toUpperCase());
		System.out.println("iskey " + column.getColumnName() + " -> " + isKey);
		return isKey;
	}

}
