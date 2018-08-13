package io.github.dvanherbergen.filelibrary;

import org.robotframework.javalib.library.AnnotationLibrary;

public class FileLibrary extends AnnotationLibrary {

	public static final String ROBOT_LIBRARY_SCOPE = "GLOBAL";

	// @formatter:off
	public static final String LIBRARY_DOCUMENTATION = "FileLibrary is a Robot Framework test library for generating test data files.\n\n"
			+ "Built in variables: ${now}, ${today}\n\n"
			+ "For SQL related commands, you can specify SQL satements either directly in the argument or provide them in an .sql file. \n\n\n"
			+ " | *IMPORTANT* | For statements included directly in the test case, you need to escape any template data variables. If you do not, it will not be able to find the value. | \n"
			+ " | _Example_ | Execute SQL	update mytable set value = 2 where id = \\${my_id} | \n";
	// @formatter:on
	
	public FileLibrary() {
		super("io/github/dvanherbergen/filelibrary/keyword/**/*.class");
	}

	@Override
	public String getKeywordDocumentation(String keywordName) {
		if (keywordName.equals("__intro__"))
			return LIBRARY_DOCUMENTATION;
		return super.getKeywordDocumentation(keywordName);
	}

}
