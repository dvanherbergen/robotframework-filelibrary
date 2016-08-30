package org.robotframework.filelibrary.remote;

import org.robotframework.filelibrary.FileLibrary;
import org.robotframework.remoteserver.RemoteServer;

public class RPCServer extends RemoteServer {

	public void run(int port) throws Exception {

		RemoteServer server = new RemoteServer();
		server.putLibrary("/RPC2", new FileLibrary());
		server.setPort(port);
		server.setAllowStop(true);
		server.start();
		Integer actualPort = server.getLocalPort();
		System.out.println("Started server on port " + actualPort + ".");

	}

	public static void main(String[] args) {

		try {
			int port = 4813;
			if (args.length == 1) {
				port = Integer.parseInt(args[0]);
			}
			System.out.println("Starting server on port " + port + " ...");
			new RPCServer().run(port);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
