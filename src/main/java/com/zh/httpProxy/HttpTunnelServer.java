package com.zh.httpProxy;

import util.ThreadUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpTunnelServer {
	private final ServerSocket server;
	public HttpTunnelServer(int port) throws IOException {
		server = new ServerSocket(port);
	}

	public void run() throws IOException {
		while (true) {
			Socket socket = server.accept();
			ThreadUtils.execute(() -> {
				try {
					new A(socket).handle();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
	}


	private static class A {

		final Socket socket;
		final InputStream clientInputStream;
		final OutputStream clientOutputStream;
		final String clientSersion;
		final int packageLength;

		A(Socket socket) throws IOException {
			this.socket = socket;
			clientInputStream = socket.getInputStream();;
			clientOutputStream = socket.getOutputStream();
			byte[] bytes = new byte[10240];
			int len = clientInputStream.read(bytes, 0, bytes.length);
			String[] contexts = new String(bytes, 0, len).split(" ");
			clientSersion = contexts[0];
			packageLength = Integer.valueOf(contexts[1]);
		}

		public void handle() {

		}

		protected void finalize(){
			HttpProxyUtil.close(socket, clientInputStream, clientOutputStream);
		}

	}

	private static class Model {

	}

}
