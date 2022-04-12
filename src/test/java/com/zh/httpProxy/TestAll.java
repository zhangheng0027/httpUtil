package com.zh.httpProxy;

import com.zh.util.ThreadUtils;
import org.junit.jupiter.api.Test;
import org.quartz.SchedulerException;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class TestAll {

	@Test
	public void test() throws InterruptedException {

		CountDownLatch cdl = new CountDownLatch(4);

		ThreadUtils.execute(() -> {
			HttpTunnelServer hts = null;
			try {
				hts = new HttpTunnelServer(12345);
				hts.run();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				cdl.countDown();
			}
		});

		Thread.sleep(500);

		ThreadUtils.execute(() -> {
			HttpTunnelClient htc = null;
			try {
				htc = new HttpTunnelClient(1234, "127.0.0.1", 12345);
				htc.run();
			} catch (IOException | InterruptedException | SchedulerException e) {
				e.printStackTrace();
			} finally {
				cdl.countDown();
			}
		});

		Thread.sleep(500);

		ThreadUtils.execute(() -> {
			try {
				new TestServer().test();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				cdl.countDown();
			}
		});

		Thread.sleep(500);

		ThreadUtils.execute(() -> {
			new TestClient().a();
			cdl.countDown();
		});

		cdl.await();
	}

}
