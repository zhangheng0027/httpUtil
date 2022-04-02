package com.zh.httpProxy;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class TestTest {

	@Test
	public void test() throws IOException {
		TestA ta = new TestA(1234);
	}

	@Test
	public void test1() throws IOException {

		URL url = new URL("http://gitee.com:443");
		URLConnection conn = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(1234)));

		try {
			Scanner scan = new Scanner(conn.getInputStream());
			StringBuilder builder = new StringBuilder();
			while (scan.hasNextLine()) {
				builder.append(scan.nextLine()).append("\n");
			}
			System.out.println(builder.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
//		Socket s = new Socket();
	}


	@Test
	public void a() {
		System.out.println("abcde".substring(3));
	}
}