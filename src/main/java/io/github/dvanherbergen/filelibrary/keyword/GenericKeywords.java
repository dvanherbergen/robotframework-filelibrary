package io.github.dvanherbergen.filelibrary.keyword;

import org.robotframework.javalib.annotation.Autowired;
import org.robotframework.javalib.annotation.RobotKeyword;
import org.robotframework.javalib.annotation.RobotKeywords;

import io.github.dvanherbergen.filelibrary.FileLibraryException;
import io.github.dvanherbergen.filelibrary.remote.RPCServer;

@RobotKeywords
public class GenericKeywords {

	@Autowired
	DatabaseKeywords dbKeywords;
	
	@RobotKeyword("Stop remote library.")
	public void stop() {

		dbKeywords.disconnect();
		try {
			RPCServer.getInstance().stop();
		} catch (Exception e) {
			throw new FileLibraryException(e);
		}
	}
	
}