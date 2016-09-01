package org.robotframework.filelibrary;

import org.robotframework.javalib.library.AnnotationLibrary;
import org.robotframework.javalib.library.KeywordDocumentationRepository;
import org.robotframework.javalib.library.RobotJavaLibrary;

public class FileLibrary implements KeywordDocumentationRepository, RobotJavaLibrary {

	public static final String ROBOT_LIBRARY_SCOPE = "GLOBAL";

	public static final String LIBRARY_DOCUMENTATION = "FileLibrary is a Robot Framework test library for generating test data files.\n"
			+ "Built in variables: now";

	private final AnnotationLibrary annotationLibrary = new AnnotationLibrary(
			"org/robotframework/filelibrary/keyword/*.class");

	@Override
	public Object runKeyword(String keywordName, Object[] args) {
		return annotationLibrary.runKeyword(keywordName, toStrings(args));
	}

	@Override
	public String[] getKeywordArguments(String keywordName) {
		return annotationLibrary.getKeywordArguments(keywordName);
	}

	@Override
	public String getKeywordDocumentation(String keywordName) {
		if (keywordName.equals("__intro__"))
			return LIBRARY_DOCUMENTATION;
		return annotationLibrary.getKeywordDocumentation(keywordName);
	}

	@Override
	public String[] getKeywordNames() {
		return annotationLibrary.getKeywordNames();
	}

	private Object[] toStrings(Object[] args) {
		Object[] newArgs = new Object[args.length];
		for (int i = 0; i < newArgs.length; i++) {
			if (args[i].getClass().isArray()) {
				newArgs[i] = args[i];
			} else {
				newArgs[i] = args[i].toString();
			}
		}
		return newArgs;
	}

}
