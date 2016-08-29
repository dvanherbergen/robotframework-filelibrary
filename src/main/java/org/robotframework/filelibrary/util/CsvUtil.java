package org.robotframework.filelibrary.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.robotframework.filelibrary.FileLibraryException;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class CsvUtil {

	public static List<Map<String, Object>> loadValuesFromFile(String filePath) {

		List<Map<String, Object>> result = new ArrayList<>();

		File csvFile = new File(filePath);
		if (!csvFile.exists()) {
			throw new FileLibraryException("Cannot find file " + csvFile.getAbsolutePath());
		}

		try {
			CsvMapper mapper = new CsvMapper();
			CsvSchema schema = CsvSchema.emptySchema().withHeader();
			MappingIterator<Map<String, Object>> it = mapper.readerFor(Map.class).with(schema).readValues(csvFile);
			while (it.hasNext()) {
				result.add(it.next());
			}
			return result;
		} catch (Exception e) {
			throw new FileLibraryException(e);
		}
	}

}
