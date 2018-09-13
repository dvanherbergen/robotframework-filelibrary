package io.github.dvanherbergen.filelibrary.remote;

import io.github.dvanherbergen.filelibrary.FileLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.BindException;

public class RPCServer extends RemoteServer {

	private static final Logger logger = LoggerFactory.getLogger(RPCServer.class);
	
	private static RPCServer instance;

	public static RPCServer getInstance() {
		return instance;
	}

	public void run() throws Exception {
        logger.info("Starting server...");
		this.putLibrary("/RPC2", new FileLibrary());
		this.setPort(0);
		this.setAllowStop(true);
		this.start();
		Integer actualPort = this.getLocalPort();
		logger.info("Started server on port " + actualPort + ".");
		System.out.println("Started server on port " + actualPort + ".");

	}

	public void stop() throws Exception {

		Runnable delayedStop = new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(500);
					server.stop();
					System.out.println("Server stopped.");
				} catch (Exception e) {
					System.out.println("Error Stopping Server ");
					e.printStackTrace();
				}
			}
		};
		new Thread(delayedStop).start();
		System.out.println("Server stop requested.");
	}

	public static void main(String[] args) {

		try {
			try {
				instance = new RPCServer();
				instance.run();
				Integer localPort = instance.getLocalPort();
				File pidFile = createPidFile(args);
				writePortToPidFile(pidFile, localPort);
			} catch (BindException e) {
				System.out.println("Cannot bind to port.");
				logger.error("Cannot bind to port.", e);
				System.exit(1);
			}
		} catch (Throwable t) {
			logger.error("Unexpected error", t);
			t.printStackTrace();
		}
	}

	private static void writePortToPidFile(File pidFile, Integer localPort) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(pidFile);
		out.write(localPort.toString());
		out.close();
	}

	private static File createPidFile(String[] args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("No pid file argument specified.");
		}
		File pidFile = new File(args[0]);
		try {
			pidFile.createNewFile();
		} catch (IOException e1) {
			System.out.println("pid file " + pidFile.getPath() + " already exists or is not writeable.");
            logger.error("Error creating pid file", e1);
			System.exit(1);
		}
		return pidFile;
	}

}
