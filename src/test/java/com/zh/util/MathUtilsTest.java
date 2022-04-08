package com.zh.util;

import com.zh.httpProxy.HttpTunnelClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;


class MathUtilsTest {
	private static final Logger log = LogManager.getLogger(MathUtilsTest.class);
	@Test
	public void a() {
		System.out.println(MathUtils.getLengthByte(255));
	}

	@Test
	public void b() {
		byte[] bs = MathUtils.int2bytes(258);
		log.info("{}, {}", bs);
	}

	@Test
	public void c() {
		int i = 127;
//		for (int i = 1; i < 10240; i++) {
			byte[] bs = MathUtils.int2bytes(i);
			if (MathUtils.bytes2int(bs, 0, bs.length) != i)
				log.info(i);
//		}
	}

}