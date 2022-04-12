package com.zh.httpProxy;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

class TestServer {

	public static final int length = 1234;

	@Test
	public void test() throws IOException {
		ServerSocket ser = new ServerSocket(4321);
		try (Socket s = ser.accept();
		OutputStream out = s.getOutputStream()) {
			System.out.println("测试服务器启动");
			for (int i = 0; i < 100; i++) {
				byte[] bytes = new byte[length];
				for (int j = 0; j < bytes.length; j++) {
					bytes[j] = (byte) (0xEF & j);
				}
//				Thread.sleep(100);
				out.write(bytes);
				out.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}