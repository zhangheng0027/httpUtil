package com.zh.httpProxy;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import static com.zh.httpProxy.TestServer.length;

public class TestClient {

	@Test
	public void a() {
		try (
				Socket s = new Socket("127.0.0.1", 1234);
				OutputStream out = s.getOutputStream();
				InputStream in = s.getInputStream()) {

			System.out.println("测试请求");
			out.write("GET http://127.0.0.1:4321 HTTP".getBytes());
			out.flush();

			byte[] bytes = new byte[length];
			int len;
			int a = 0;
			while ((len = in.read(bytes, 0, bytes.length)) != -1) {

//				if (len != length)
//					throw new Error("长度不对" + len);
				a += len;
				System.out.println(a);
//
//				for (int i = 0; i < len; i++) {
//					if (((byte) (0xEF & i)) != bytes[i])
//						throw new Error("内容错误");
//				}

			}


		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
