package org.robotframework.filelibrary.remote;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.BindException;
import java.util.UUID;

import org.robotframework.filelibrary.FileLibrary;
import org.robotframework.remoteserver.RemoteServer;

public class RPCServer extends RemoteServer {

	private static RPCServer instance;

	public static RPCServer getInstance() {
		return instance;
	}

	public void run() throws Exception {

		this.putLibrary("/RPC2", new FileLibrary());
		this.setPort(0);
		this.setAllowStop(true);
		this.start();
		Integer actualPort = this.getLocalPort();
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
				System.exit(1);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private static void writePortToPidFile(File pidFile, Integer localPort) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(pidFile);
		out.write(localPort.toString());
		out.close();
	}

	private static File createPidFile(String[] args) {
		String uniqueId;
		if (args.length != 1) {
			uniqueId = UUID.randomUUID().toString();
		} else {
			uniqueId = args[0];
		}
		File pidFile = new File(uniqueId + ".pid");

		try {
			pidFile.createNewFile();
		} catch (IOException e1) {
			System.out.println("pid file " + pidFile.getPath() + " already exists.");
			System.exit(1);
		}
		return pidFile;
	}

}
