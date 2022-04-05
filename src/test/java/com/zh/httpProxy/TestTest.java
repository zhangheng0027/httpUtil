package com.zh.httpProxy;

import com.zh.util.ArrayUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class TestTest {

	@Test
	public void test() throws IOException {
		byte[] b = {1,2,3,4,5,5};
		Byte[] bs = new Byte[b.length];
		ArrayUtils.byte2Byte(b, 0, bs, 1, b.length -1);
		System.out.println(bs[1]);
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