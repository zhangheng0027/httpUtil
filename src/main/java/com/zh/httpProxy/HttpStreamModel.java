package com.zh.httpProxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpStreamModel implements java.io.Closeable {

	private final Socket socket;
	private OutputStream outputStream;
	private InputStream inputStream;

	public HttpStreamModel(Socket socket) {
		this.socket = socket;
	}

	public synchronized OutputStream getOutputStream() throws IOException {
		if (outputStream == null)
			this.outputStream = socket.getOutputStream();
		return outputStream;
	}

	public synchronized InputStream getInputStream() throws IOException {
		if (inputStream == null)
			this.inputStream = socket.getInputStream();
		return inputStream;
	}

	@Override
	public void close() throws IOException {
		HttpProxyUtil.close(socket, outputStream, inputStream);
	}
}
