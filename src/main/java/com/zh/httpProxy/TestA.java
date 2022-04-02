package com.zh.httpProxy;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestA {
	private final ServerSocket server;

	private final static ThreadPoolExecutor pool = new
			ThreadPoolExecutor(8, 32, 200, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(32));
	public TestA(int port) throws IOException {
		System.out.println("监听端口 " + port);
		server = new ServerSocket(port);
		while (true) {
			final Socket socket = server.accept();
			pool.execute(() -> {
				try {
					handleSocket(socket);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public void handleSocket(Socket socket) throws IOException {

		try(InputStream clientInputStream = socket.getInputStream();
			OutputStream clientOutputStream = socket.getOutputStream()) {
			byte[] bytes = new byte[10240];

			int len = clientInputStream.read(bytes, 0, bytes.length);
			if (len == 0)
				return;
			String context = new String(bytes, 0, len);
			if (context.startsWith("CONNECT")) {
				handleContext(clientInputStream, clientOutputStream, context);
			} else {

			}
			System.out.println(context);
//			HttpURLConnection connection = parseServerHost(context);

		}

	}

	public void handle(InputStream clientInputStream, OutputStream clientOutputStream, String context) throws IOException {
		String [] con = context.split(" ");
		String host = con[1];
		if (host.toUpperCase().startsWith("HTTP://"))
			host = host.substring(7);
		int port = 80;
		if (host.contains(":")) {
			port = Integer.parseInt(host.substring(host.indexOf(":") + 1));
			host = host.substring(0, host.indexOf(":"));
		}
		Socket server = new Socket(host, port);

	}

	/**
	 * 处理 connect 请求
	 * @param clientInputStream
	 * @param clientOutputStream
	 * @param context
	 */
	public void handleContext(InputStream clientInputStream, OutputStream clientOutputStream, String context) throws IOException {
		clientOutputStream.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
		String [] con = context.split(" ");
		String host = con[1];
		if (!host.toUpperCase().startsWith("HTTP://"))
			host = "http://" + host;
		URL url = new URL(host);
		final HttpURLConnection connection = (HttpURLConnection)url.openConnection();

		pool.execute(() -> {
//			connection.getInputStream()
		});

	}

}
