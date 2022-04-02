package com.zh.httpProxy;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class HttpProxyUtilTest {

	@Test
	public void aa() throws IOException {
		new HttpProxyUtil(1234).run();
	}
}