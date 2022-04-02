package util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadUtils {

	private final static ThreadPoolExecutor pool = new
			ThreadPoolExecutor(8, 32, 200, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(32));

	public static void execute(Runnable command) {
		pool.execute(command);
	}

}
