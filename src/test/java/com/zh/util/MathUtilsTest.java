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
		byte[] bs = MathUtils.int2bytes(129);
		log.info("{}, {}", bs);
	}

	@Test
	public void c() {
		for (int i = 1; i < 10240; i++) {
			byte[] bs = MathUtils.int2bytes(i);
			if (MathUtils.bytes2int(bs, 0, bs.length) != i) {
				log.info("{} {}",i, bs);
				return;
			}
		}
	}

	@Test
	public void d() {
		int[] a = {1,2,3,4,5,6,7,8,9,0};
		System.arraycopy(a, 3, a, 0, 7);
		log.info("{}", a);
	}

	@Test
	public void e() {
		int i = 129;
		System.out.println(0xFF & ((byte)i));
	}

}