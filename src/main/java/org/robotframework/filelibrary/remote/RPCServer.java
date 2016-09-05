package org.robotframework.filelibrary.remote;

import java.io.File;
import java.net.BindException;

import org.robotframework.filelibrary.FileLibrary;
import org.robotframework.remoteserver.RemoteServer;

public class RPCServer extends RemoteServer {

	private static RPCServer instance;

	private RemoteServer server;

	public static final String FLAG_FILE = "file-library.pid";

	public static RPCServer getInstance() {
		return instance;
	}

	public void run(int port) throws Exception {

		server = new RemoteServer();
		server.putLibrary("/RPC2", new FileLibrary());
		server.setPort(port);
		server.setAllowStop(true);
		server.start();
		Integer actualPort = server.getLocalPort();
		System.out.println("Started server on port " + actualPort + ".");
		new File(FLAG_FILE).createNewFile();

	}

	public void stop() throws Exception {

		Runnable delayedStop = new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(500);
					server.stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		new File(FLAG_FILE).deleteOnExit();
		new Thread(delayedStop).start();
		System.out.println("Server stop requested.");

	}

	public static void main(String[] args) {

		try {
			int port = 4813;
			if (args.length == 1) {
				port = Integer.parseInt(args[0]);
			}
			System.out.println("Starting server on port " + port + " ...");
			try {
				instance = new RPCServer();
				instance.run(port);
			} catch (BindException e) {
				System.out.println("Cannot bind to port. There is probably another instance running.");
				System.exit(1);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
